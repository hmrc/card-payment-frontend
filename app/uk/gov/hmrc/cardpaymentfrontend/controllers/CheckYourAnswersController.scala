/*
 * Copyright 2024 HM Revenue & Customs
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
import uk.gov.hmrc.cardpaymentfrontend.connectors.CardPaymentConnector
import uk.gov.hmrc.cardpaymentfrontend.models.CheckYourAnswersRow
import uk.gov.hmrc.cardpaymentfrontend.models.CheckYourAnswersRow.summarise
import uk.gov.hmrc.cardpaymentfrontend.models.extendedorigins.ExtendedOrigin
import uk.gov.hmrc.cardpaymentfrontend.models.extendedorigins.ExtendedOrigin.OriginExtended
import uk.gov.hmrc.cardpaymentfrontend.requests.RequestSupport
import uk.gov.hmrc.cardpaymentfrontend.views.html.CheckYourAnswersPage
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton()
class CheckYourAnswersController @Inject() (
    actions:              Actions,
    cardPaymentConnector: CardPaymentConnector, //todo introduce a service layer maybe
    checkYourAnswersPage: CheckYourAnswersPage,
    mcc:                  MessagesControllerComponents,
    requestSupport:       RequestSupport
)(implicit executionContext: ExecutionContext) extends FrontendController(mcc) {
  import requestSupport._

  def renderPage: Action[AnyContent] = actions.journeyAction { implicit journeyRequest: JourneyRequest[AnyContent] =>
    val extendedOrigin: ExtendedOrigin = journeyRequest.journey.origin.lift

    val paymentDate: Option[CheckYourAnswersRow] = extendedOrigin.checkYourAnswersPaymentDateRow(journeyRequest)
    val referenceRow: Option[CheckYourAnswersRow] = extendedOrigin.checkYourAnswersReferenceRow(journeyRequest)
    val additionalReferenceRow: Option[CheckYourAnswersRow] = extendedOrigin.checkYourAnswersAdditionalReferenceRow(journeyRequest)
    val amountRow: Option[CheckYourAnswersRow] = extendedOrigin.checkYourAnswersAmountSummaryRow(journeyRequest)
    val maybeEmailRow: Option[CheckYourAnswersRow] = extendedOrigin.checkYourAnswersEmailAddressRow(journeyRequest)
    val cardBillingAddressRow: Option[CheckYourAnswersRow] = extendedOrigin.checkYourAnswersCardBillingAddressRow(journeyRequest)

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

  def submit: Action[AnyContent] = actions.journeyAction.async { implicit request: JourneyRequest[AnyContent] =>
    cardPaymentConnector
      .initiatePayment()(requestSupport.hc)
      .map { cardPaymentInitiatePaymentResponse =>
        Redirect(routes.PaymentStatusController.showIframe(cardPaymentInitiatePaymentResponse.redirectUrl))
      }
  }

}
