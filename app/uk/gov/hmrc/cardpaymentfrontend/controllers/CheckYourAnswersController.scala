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
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.cardpaymentfrontend.actions.{Actions, JourneyRequest}
import uk.gov.hmrc.cardpaymentfrontend.config.AppConfig
import uk.gov.hmrc.cardpaymentfrontend.models.CheckYourAnswersRow.summarise
import uk.gov.hmrc.cardpaymentfrontend.models.extendedorigins.ExtendedOrigin
import uk.gov.hmrc.cardpaymentfrontend.models.extendedorigins.ExtendedOrigin.OriginExtended
import uk.gov.hmrc.cardpaymentfrontend.models.{Address, CheckYourAnswersRow, EmailAddress}
import uk.gov.hmrc.cardpaymentfrontend.requests.RequestSupport
import uk.gov.hmrc.cardpaymentfrontend.services.CardPaymentService
import uk.gov.hmrc.cardpaymentfrontend.session.JourneySessionSupport.*
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
)(implicit executionContext: ExecutionContext)
    extends FrontendController(mcc)
    with Logging {
  import requestSupport.*

  def renderPage: Action[AnyContent] = actions.journeyAction { implicit journeyRequest: JourneyRequest[AnyContent] =>
    val extendedOrigin: ExtendedOrigin = journeyRequest.journey.origin.lift(appConfig)

    val maybePaymentDate: Option[CheckYourAnswersRow]             = extendedOrigin.checkYourAnswersPaymentDateRow(journeyRequest)(appConfig.payFrontendBaseUrl)
    val referenceRow: Option[CheckYourAnswersRow]                 = extendedOrigin.checkYourAnswersReferenceRow(journeyRequest)(appConfig.payFrontendBaseUrl)
    val additionalReferenceRows: Option[Seq[CheckYourAnswersRow]] =
      extendedOrigin.checkYourAnswersAdditionalReferenceRow(journeyRequest)(appConfig.payFrontendBaseUrl)
    val amountRow: Option[CheckYourAnswersRow]                    = extendedOrigin.checkYourAnswersAmountSummaryRow(journeyRequest)(appConfig.payFrontendBaseUrl)
    val cardBillingAddressRow: Option[CheckYourAnswersRow]        = extendedOrigin.checkYourAnswersCardBillingAddressRow(journeyRequest)
    // If no email is present in the session, no Email Row is shown
    val maybeEmailRow: Option[CheckYourAnswersRow]                = extendedOrigin.checkYourAnswersEmailAddressRow(journeyRequest)

    // the first two rows, payment date only applies to FDP supported journeys; reference row should always be there.
    val maybePaymentDateAndReferenceRows: Seq[Option[CheckYourAnswersRow]] = Seq(maybePaymentDate, referenceRow)

    // essentially converts Option[Seq[CheckYourAnswersRow]] => Seq[Option[CheckYourAnswersRow]], in most cases it's empty seq
    val maybeAdditionalReferenceRows: Seq[Option[CheckYourAnswersRow]] =
      additionalReferenceRows
        .map(additionalReferenceRows => additionalReferenceRows.map(Option(_)))
        .getOrElse(Seq.empty[Option[CheckYourAnswersRow]])

    // the other rows should always be there (although email row isn't, if they didn't enter an email).
    val mandatoryRows: Seq[Option[CheckYourAnswersRow]] = Seq(amountRow, maybeEmailRow, cardBillingAddressRow)

    /*
     * Looks weird, but it's so we can display rows in correct order i.e. usually:
     * - payment date (for fdp supported journeys)
     * - reference
     * - additional references/identifiers
     * - amount, email (if entered), card billing address)
     */
    def summaryListRows: Seq[SummaryListRow] =
      (maybePaymentDateAndReferenceRows ++ maybeAdditionalReferenceRows ++ mandatoryRows).flatten
        .map(summarise)

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
              journey = journeyRequest.journey,
              addressFromSession = address,
              maybeEmailFromSession = journeyRequest.readFromSession[EmailAddress](journeyRequest.journeyId, Keys.email),
              language = requestSupport.usableLanguage
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
              case None        =>
                logger.warn(s"Payment status for journeyId ${journeyRequest.journeyId.toString} was Sent but order was None.")
                initiatePayment()
            }
          case _                    =>
            initiatePayment()
        }

      case None =>
        logger.warn("Missing address from session, redirecting to enter address page.")
        Future.successful(Redirect(routes.AddressController.renderPage))
    }
  }

}
