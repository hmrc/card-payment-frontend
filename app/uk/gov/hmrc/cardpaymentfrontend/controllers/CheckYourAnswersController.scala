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

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.cardpaymentfrontend.actions.{Actions, JourneyRequest}
import uk.gov.hmrc.cardpaymentfrontend.config.AppConfig
import uk.gov.hmrc.cardpaymentfrontend.models.{Address, CheckYourAnswersRow, EmailAddress}
import uk.gov.hmrc.cardpaymentfrontend.models.CheckYourAnswersRow.summarise
import uk.gov.hmrc.cardpaymentfrontend.models.extendedorigins.ExtendedOrigin
import uk.gov.hmrc.cardpaymentfrontend.models.extendedorigins.ExtendedOrigin.OriginExtended
import uk.gov.hmrc.cardpaymentfrontend.requests.RequestSupport
import uk.gov.hmrc.cardpaymentfrontend.services.CardPaymentService
import uk.gov.hmrc.cardpaymentfrontend.session.JourneySessionSupport._
import uk.gov.hmrc.cardpaymentfrontend.views.html.CheckYourAnswersPage
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton()
class CheckYourAnswersController @Inject() (
    actions:              Actions,
    appConfig:            AppConfig,
    cardPaymentService:   CardPaymentService,
    checkYourAnswersPage: CheckYourAnswersPage,
    mcc:                  MessagesControllerComponents,
    requestSupport:       RequestSupport
)(implicit executionContext: ExecutionContext) extends FrontendController(mcc) {
  import requestSupport._

  def renderPage: Action[AnyContent] = actions.journeyAction { implicit journeyRequest: JourneyRequest[AnyContent] =>
    val extendedOrigin: ExtendedOrigin = journeyRequest.journey.origin.lift

    val paymentDate: Option[CheckYourAnswersRow] = extendedOrigin.checkYourAnswersPaymentDateRow(journeyRequest)(appConfig.payFrontendBaseUrl)
    val referenceRow: Option[CheckYourAnswersRow] = extendedOrigin.checkYourAnswersReferenceRow(journeyRequest)(appConfig.payFrontendBaseUrl)
    val additionalReferenceRow: Option[CheckYourAnswersRow] = extendedOrigin.checkYourAnswersAdditionalReferenceRow(journeyRequest)
    val amountRow: Option[CheckYourAnswersRow] = extendedOrigin.checkYourAnswersAmountSummaryRow(journeyRequest)(appConfig.payFrontendBaseUrl)
    val cardBillingAddressRow: Option[CheckYourAnswersRow] = extendedOrigin.checkYourAnswersCardBillingAddressRow(journeyRequest)
    // If no email is present in the session, no Email Row is shown
    val maybeEmailRow: Option[CheckYourAnswersRow] = extendedOrigin.checkYourAnswersEmailAddressRow(journeyRequest)

    val summaryListRows: Seq[SummaryListRow] = Seq(
      paymentDate,
      referenceRow,
      additionalReferenceRow,
      amountRow,
      maybeEmailRow,
      cardBillingAddressRow
    ).flatten.map(summarise)

    Ok(checkYourAnswersPage(SummaryList(summaryListRows)))
  }

  def submit: Action[AnyContent] = actions.journeyAction.async { implicit journeyRequest: JourneyRequest[AnyContent] =>
    cardPaymentService
      .initiatePayment(
        journey               = journeyRequest.journey,
        addressFromSession    = journeyRequest.readFromSession[Address](journeyRequest.journeyId, Keys.address).getOrElse(throw new RuntimeException("We can't process a card payment without the billing address.")),
        maybeEmailFromSession = journeyRequest.readFromSession[EmailAddress](journeyRequest.journeyId, Keys.email),
        language              = requestSupport.usableLanguage
      )(requestSupport.hc)
      .map { cardPaymentInitiatePaymentResponse =>
        Redirect(routes.PaymentStatusController.showIframe(RedirectUrl(cardPaymentInitiatePaymentResponse.redirectUrl)))
      }
  }

}
