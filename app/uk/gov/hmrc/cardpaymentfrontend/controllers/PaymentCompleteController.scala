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

import payapi.cardpaymentjourney.model.journey.{Journey, JourneySpecificData}
import payapi.corcommon.model.barclays.CardCategories
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import uk.gov.hmrc.cardpaymentfrontend.actions.{Actions, JourneyRequest}
import uk.gov.hmrc.cardpaymentfrontend.models.{EmailAddress, creditCardCommissionRate}
import uk.gov.hmrc.cardpaymentfrontend.requests.RequestSupport
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.cardpaymentfrontend.views.html.PaymentCompletePage

import javax.inject.Inject
import uk.gov.hmrc.cardpaymentfrontend.session.JourneySessionSupport._
import uk.gov.hmrc.cardpaymentfrontend.utils.{DateStringBuilder, OriginExtraInfo}
import uk.gov.hmrc.govukfrontend.views.Aliases.{HtmlContent, Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow}

class PaymentCompleteController @Inject() (
    actions:             Actions,
    originExtraInfo:     OriginExtraInfo,
    mcc:                 MessagesControllerComponents,
    paymentCompletePage: PaymentCompletePage,
    requestSupport:      RequestSupport
) extends FrontendController(mcc) {

  import requestSupport._

  def renderPage: Action[AnyContent] = actions.journeyAction { implicit request: JourneyRequest[AnyContent] =>

    println("----------------")
    println(request.journey.origin.toString)
    println("-------LIFTED ORIGIN---------")
    println(originExtraInfo.lift(request.journey.origin).taxNameMessageKey)

    val maybeEmailFromSession: Option[EmailAddress] =
      request.readFromSession[EmailAddress](request.journeyId, Keys.email).map(email => EmailAddress(email.value))

    Ok(paymentCompletePage(
      taxReference           = request.journey.getReference,
      taxType                = Messages(originExtraInfo.lift(request.journey.origin).taxNameMessageKey),
      amountsSummaryListRows = PaymentCompleteController.buildAmountsSummaryListRow(request.journey),
      paymentDate            = DateStringBuilder.getDateAsString(request.journey.getPaidOn),
      maybeEmailAddress      = maybeEmailFromSession
    ))
  }
}

object PaymentCompleteController {

  def buildAmountsSummaryListRow(journey: Journey[JourneySpecificData])(implicit messages: Messages): Seq[SummaryListRow] = {

    val basicAmount = SummaryListRow(
      key   = Key(Text(messages("payment-complete.summary-list.amount"))),
      value = Value(Text(s"£${journey.getTotalAmountInPence.inPoundsRoundedFormatted}"))
    )

    journey.getCardCategory match {

      case CardCategories.debit => Seq(basicAmount)

      case CardCategories.credit =>
        journey.getCommissionInPence.fold(Seq(basicAmount)) { commissionInPence =>
          Seq(
            SummaryListRow(
              key   = Key(Text(Messages("payment-complete.amount.paid-to-hmrc"))),
              value = Value(content = Text(s"£${journey.getAmountInPence.inPoundsRoundedFormatted}"))
            ),
            SummaryListRow(
              key   = Key(HtmlContent(Html(Messages("payment-complete.amount.card-fee", "<nobr>", "%2.2f%%".format(creditCardCommissionRate(journey.getAmountInPence, commissionInPence)), "</nobr><br/><nobr>", "</nobr>")))),
              value = Value(content = Text(s"£${commissionInPence.inPoundsRoundedFormatted}"))
            ),
            SummaryListRow(
              key   = Key(Text(messages("payment-complete.amount.total-paid"))),
              value = Value(Text(s"£${journey.getTotalAmountInPence.inPoundsRoundedFormatted}"))
            )
          )
        }
    }
  }

}
