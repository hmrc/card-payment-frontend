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

import payapi.cardpaymentjourney.model.journey.{Journey, JourneySpecificData, JsdAlcoholDuty, JsdPfP800, JsdPtaP800, JsdPtaSimpleAssessment}
import payapi.corcommon.model.barclays.CardCategories
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import uk.gov.hmrc.cardpaymentfrontend.actions.{Actions, JourneyRequest}
import uk.gov.hmrc.cardpaymentfrontend.config.AppConfig
import uk.gov.hmrc.cardpaymentfrontend.models.extendedorigins.ExtendedOrigin.OriginExtended
import uk.gov.hmrc.cardpaymentfrontend.models.{EmailAddress, creditCardCommissionRate}
import uk.gov.hmrc.cardpaymentfrontend.requests.RequestSupport
import uk.gov.hmrc.cardpaymentfrontend.session.JourneySessionSupport._
import uk.gov.hmrc.cardpaymentfrontend.util.DateStringBuilder
import uk.gov.hmrc.cardpaymentfrontend.views.html.PaymentCompletePage
import uk.gov.hmrc.govukfrontend.views.Aliases.{HtmlContent, Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject

class PaymentCompleteController @Inject() (
    actions:             Actions,
    appConfig:           AppConfig,
    mcc:                 MessagesControllerComponents,
    paymentCompletePage: PaymentCompletePage,
    requestSupport:      RequestSupport
) extends FrontendController(mcc) {

  import requestSupport._

  val renderPage: Action[AnyContent] = actions.journeyAction { implicit journeyRequest: JourneyRequest[AnyContent] =>

    val maybeEmailFromSession: Option[EmailAddress] =
      journeyRequest.readFromSession[EmailAddress](journeyRequest.journeyId, Keys.email).filter(!_.value.isBlank).map(email => EmailAddress(email.value))

    Ok(paymentCompletePage(
      taxReference      = journeyRequest.journey.getReference,
      summaryListRows   = PaymentCompleteController.buildSummaryListRows(
        journey = journeyRequest.journey,
        taxType = journeyRequest.journey.origin.lift.taxNameMessageKey
      ),
      maybeEmailAddress = maybeEmailFromSession,
      maybeReturnUrl    = PaymentCompleteController.determineTaxAccountUrl(journeyRequest.journey)(appConfig)
    ))
  }
}

object PaymentCompleteController {

  def buildSummaryListRows(journey: Journey[JourneySpecificData], taxType: String)(implicit messages: Messages): Seq[SummaryListRow] = {
    val consistentRows = Seq(
      SummaryListRow(
        key   = Key(Text(messages("payment-complete.summary-list.tax"))),
        value = Value(Text(messages(taxType)))
      ),
      SummaryListRow(
        key   = Key(Text(messages("payment-complete.summary-list.date"))),
        value = Value(Text(messages(DateStringBuilder.getDateAsString(journey.getPaidOn))))
      )
    )

    val taxSpecificRows: Seq[SummaryListRow] = journey.journeySpecificData match {
      case JsdAlcoholDuty(_, alcoholDutyChargeReference, _) =>
        alcoholDutyChargeReference.fold[Seq[SummaryListRow]](Seq.empty[SummaryListRow]) { alcoholDutyChargeReference =>
          Seq(
            SummaryListRow(
              key   = Key(Text(messages("check-your-details.AlcoholDuty.charge-reference"))),
              value = Value(Text(alcoholDutyChargeReference.canonicalizedValue))
            )
          )
        }
      case JsdPfP800(p800Ref, p800ChargeRef, _) =>
        p800ChargeRef.fold[Seq[SummaryListRow]](Seq.empty[SummaryListRow]) { chargeRef =>
          Seq(
            SummaryListRow(
              key   = Key(Text(messages("check-your-details.PfP800.charge-reference"))),
              value = Value(Text(chargeRef.canonicalizedValue))
            )
          )
        } ++ Seq(SummaryListRow(
          key   = Key(Text(messages("check-your-details.PfP800.reference"))),
          value = Value(Text(p800Ref.canonicalizedValue))
        ))

      case JsdPtaP800(p800Ref, p800ChargeRef, _, _) =>
        p800ChargeRef.fold[Seq[SummaryListRow]](Seq.empty[SummaryListRow]) { chargeRef =>
          Seq(
            SummaryListRow(
              key   = Key(Text(messages("check-your-details.PtaP800.charge-reference"))),
              value = Value(Text(chargeRef.canonicalizedValue))
            )
          )
        } ++ Seq(SummaryListRow(
          key   = Key(Text(messages("check-your-details.PtaP800.reference"))),
          value = Value(Text(p800Ref.canonicalizedValue))
        ))

      case JsdPtaSimpleAssessment(_, _, _, _, _) =>
        Seq.empty

      case _ => Seq.empty[SummaryListRow]
    }

    val amountRows = buildAmountsSummaryListRow(journey)

    consistentRows ++ taxSpecificRows ++ amountRows
  }

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

  private def determineTaxAccountUrl(journey: Journey[_])(appConfig: AppConfig): Option[String] = {
    if (journey.origin.isAWebChatOrigin) {
      Some(appConfig.businessTaxAccountUrl)
    } else journey.navigation.flatMap(_.returnUrl.map(_.value))
  }

}
