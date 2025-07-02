/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.cardpaymentfrontend.controllers

import payapi.corcommon.model.PaymentStatuses
import play.api.Logging
import play.api.i18n.Lang
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.cardpaymentfrontend.actions.{Actions, JourneyRequest}
import uk.gov.hmrc.cardpaymentfrontend.config.AppConfig
import uk.gov.hmrc.cardpaymentfrontend.models.CheckYourAnswersRow.summarise
import uk.gov.hmrc.cardpaymentfrontend.models.extendedorigins.ExtendedOrigin
import uk.gov.hmrc.cardpaymentfrontend.models.extendedorigins.ExtendedOrigin.OriginExtended
import uk.gov.hmrc.cardpaymentfrontend.models.{Address, CheckYourAnswersRow, EmailAddress}
import uk.gov.hmrc.cardpaymentfrontend.requests.RequestSupport
import uk.gov.hmrc.cardpaymentfrontend.services.CardPaymentService
import uk.gov.hmrc.cardpaymentfrontend.session.JourneySessionSupport._
import uk.gov.hmrc.cardpaymentfrontend.views.html.CheckYourAnswersPage
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class CheckYourAnswersController @Inject() (
    actions:              Actions,
    appConfig:            AppConfig,
    cardPaymentService:   CardPaymentService,
    checkYourAnswersPage: CheckYourAnswersPage,
    mcc:                  MessagesControllerComponents,
    requestSupport:       RequestSupport
)(implicit executionContext: ExecutionContext) extends FrontendController(mcc) with Logging {
  import requestSupport._

  def renderPage: Action[AnyContent] = actions.journeyAction { implicit journeyRequest: JourneyRequest[AnyContent] =>
    implicit val lang: Lang = requestSupport.lang
    val extendedOrigin: ExtendedOrigin = journeyRequest.journey.origin.lift

    val paymentDate: Option[CheckYourAnswersRow] = extendedOrigin.checkYourAnswersPaymentDateRow(journeyRequest)(appConfig.payFrontendBaseUrl)
    val referenceRow: Option[CheckYourAnswersRow] = extendedOrigin.checkYourAnswersReferenceRow(journeyRequest)(appConfig.payFrontendBaseUrl)
    val additionalReferenceRow: Option[CheckYourAnswersRow] = extendedOrigin.checkYourAnswersAdditionalReferenceRow(journeyRequest)(appConfig.payFrontendBaseUrl)
    val amountRow: Option[CheckYourAnswersRow] = extendedOrigin.checkYourAnswersAmountSummaryRow(journeyRequest)(appConfig.payFrontendBaseUrl)
    val cardBillingAddressRow: Option[CheckYourAnswersRow] = extendedOrigin.checkYourAnswersCardBillingAddressRow(journeyRequest)
    // If no email is present in the session, no Email Row is shown
    val maybeEmailRow: Option[CheckYourAnswersRow] = extendedOrigin.checkYourAnswersEmailAddressRow(journeyRequest)

      def summaryListRows: Seq[SummaryListRow] = Seq(
        paymentDate,
        referenceRow,
        additionalReferenceRow,
        amountRow,
        maybeEmailRow,
        cardBillingAddressRow
      ).flatten.map(summarise)

    // if there is no address, we shouldn't error,instead redirect user to the
    if (cardBillingAddressRow.isDefined) Ok(checkYourAnswersPage(SummaryList(summaryListRows)))
    else {
      logger.warn("Missing address from session, redirecting to enter address page.")
      Redirect(routes.AddressController.renderPage)
    }
  }

  def submit: Action[AnyContent] = actions.journeyAction.async { implicit journeyRequest: JourneyRequest[AnyContent] =>
    journeyRequest.readFromSession[Address](journeyRequest.journeyId, Keys.address) match {
      case Some(address) =>
        def initiatePayment(): Future[Result] = {
            cardPaymentService
              .initiatePayment(
                journey               = journeyRequest.journey,
                addressFromSession    = address,
                maybeEmailFromSession = journeyRequest.readFromSession[EmailAddress](journeyRequest.journeyId, Keys.email),
                language              = requestSupport.usableLanguage
              )(requestSupport.hc, journeyRequest)
              .map { response =>
                Redirect(routes.PaymentStatusController.showIframe(RedirectUrl(response.redirectUrl)))
              }
          }
        // If PaymentStatus is Sent then Redirect to the iFrameUrl from the order.
        journeyRequest.journey.status match {
          case PaymentStatuses.Sent =>
            journeyRequest.journey.order match {
              case Some(order) =>
                Future.successful(Redirect(routes.PaymentStatusController.showIframe(RedirectUrl(order.iFrameUrl.value))))
              case None =>
                logger.warn(s"Payment status for journeyId ${journeyRequest.journeyId.toString} was Sent but order was None.")
                initiatePayment()
            }
          case _ =>
            initiatePayment()
        }

      case None =>
        Future.successful(Redirect(routes.AddressController.renderPage))
    }
  }

}
