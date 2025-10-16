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

import payapi.cardpaymentjourney.model.journey._
import payapi.corcommon.model.{AmountInPence, Origins}
import payapi.corcommon.model.barclays.CardCategories
import payapi.corcommon.model.taxes.pngr.AmountPaidPreviously
import payapi.corcommon.model.times.period.TaxYear
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.cardpaymentfrontend.actions.{Actions, JourneyRequest}
import uk.gov.hmrc.cardpaymentfrontend.config.AppConfig
import uk.gov.hmrc.cardpaymentfrontend.models.extendedorigins.ExtendedOrigin.OriginExtended
import uk.gov.hmrc.cardpaymentfrontend.models.{EmailAddress, creditCardCommissionRate}
import uk.gov.hmrc.cardpaymentfrontend.requests.RequestSupport
import uk.gov.hmrc.cardpaymentfrontend.session.JourneySessionSupport._
import uk.gov.hmrc.cardpaymentfrontend.util.DateStringBuilder
import uk.gov.hmrc.cardpaymentfrontend.util.SafeEquals.EqualsOps
import uk.gov.hmrc.cardpaymentfrontend.views.html.{PassengersPaymentCompletePage, PaymentCompletePage}
import uk.gov.hmrc.govukfrontend.views.Aliases._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import java.time.{Clock, LocalDateTime}
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class PaymentCompleteController @Inject() (
    actions:                       Actions,
    appConfig:                     AppConfig,
    clock:                         Clock,
    mcc:                           MessagesControllerComponents,
    paymentCompletePage:           PaymentCompletePage,
    passengersPaymentCompletePage: PassengersPaymentCompletePage,
    requestSupport:                RequestSupport
) extends FrontendController(mcc) {

  import requestSupport._

  val renderPage: Action[AnyContent] = actions.journeyAction { implicit journeyRequest: JourneyRequest[AnyContent] =>

    val maybeEmailFromSession: Option[EmailAddress] =
      journeyRequest.readFromSession[EmailAddress](journeyRequest.journeyId, Keys.email).filter(!_.value.isBlank).map(email => EmailAddress(email.value))

    journeyRequest.journey.journeySpecificData match {
      //passengers has a bespoke set of content, so they have their own page for simplicity
      case jsd: JsdBcPngr         => Ok(passengersPaymentCompletePage(jsd))

      // all other origins utilise the generic page.
      case _: JourneySpecificData => Ok(genericPaymentCompletePage(maybeEmailFromSession))
    }
  }

  private def genericPaymentCompletePage(maybeEmail: Option[EmailAddress])(implicit journeyRequest: JourneyRequest[_]): HtmlFormat.Appendable = {
    paymentCompletePage(
      taxReference      = journeyRequest.journey.getReference,
      summaryListRows   = PaymentCompleteController.buildSummaryListRows(
        journey = journeyRequest.journey,
        taxType = journeyRequest.journey.origin.lift.taxNameMessageKey
      )(journeyRequest.messages),
      maybeEmailAddress = maybeEmail,
      maybeReturnUrl    = PaymentCompleteController.determineTaxAccountUrl(journeyRequest.journey)(appConfig)
    )(journeyRequest, journeyRequest.messages)
  }

  private def passengersPaymentCompletePage(jsd: JsdBcPngr)(implicit journeyRequest: JourneyRequest[_]): HtmlFormat.Appendable = {
    passengersPaymentCompletePage(
      taxReference          = journeyRequest.journey.getReference,
      leadingSummaryTable   = PaymentCompleteController.buildPassengersSummaryTable(jsd, journeyRequest.journey.getCommissionInPence.getOrElse(AmountInPence.zero), journeyRequest.journey.getAmountInPence)(clock),
      itemsDeclaredTable    = PaymentCompleteController.buildPassengersItemsDeclaredTable(jsd),
      paymentBreakdownTable = PaymentCompleteController.buildPassengersTaxBreakdown(jsd),
    )
  }
}

object PaymentCompleteController {

  def buildSummaryListRows(journey: Journey[JourneySpecificData], taxType: String)(implicit messages: Messages): Seq[SummaryListRow] = {
    val consistentRows = Seq(
      SummaryListRow(
        key   = Key(Text(messages({ if (journey.origin === Origins.Mib) "payment-complete.summary-list.payment" else "payment-complete.summary-list.tax" }))),
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

      case JsdPtaSimpleAssessment(_, p302ChargeRef, taxYear, _, _) =>
        val adjustedTaxYear: TaxYear = taxYear.nextTaxYear
        Seq(
          SummaryListRow(
            key   = Key(Text(messages("check-your-details.PtaSimpleAssessment.charge-reference"))),
            value = Value(Text(p302ChargeRef.canonicalizedValue))
          ),
          SummaryListRow(
            key   = Key(Text(messages("check-your-details.PtaSimpleAssessment.tax-year"))),
            value = Value(Text(messages("check-your-details.PtaSimpleAssessment.tax-year.value", adjustedTaxYear.startYear.toString, adjustedTaxYear.endYear.toString)))
          )
        )

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

  private def buildPassengersSummaryTable(journeySpecificData: JsdBcPngr, commissionAmount: AmountInPence, totalAmount: AmountInPence)(clock: Clock)(implicit messages: Messages): Seq[Seq[TableRow]] = {
    val amountsTableRows: Seq[Seq[TableRow]] = {
      if (commissionAmount > AmountInPence.zero) {
        Seq(
          Seq(
            TableRow(classes = "govuk-!-font-weight-bold", content = Text(messages("payment-complete.amount.paid-to-hmrc"))),
            TableRow(content = Text(s"£${totalAmount.inPoundsRoundedFormatted}"))
          ),
          Seq(
            TableRow(classes = "govuk-!-font-weight-bold", content = HtmlContent(Html(Messages("payment-complete.amount.card-fee", "<nobr>", "%2.2f%%".format(creditCardCommissionRate(totalAmount, commissionAmount)), "</nobr><br/><nobr>", "</nobr>")))),
            TableRow(content = Text(s"£${commissionAmount.inPoundsRoundedFormatted}"))
          ),
          Seq(
            TableRow(classes = "govuk-!-font-weight-bold", content = Text(messages("payment-complete.amount.total-paid"))),
            TableRow(content = Text(s"£${(commissionAmount + totalAmount).inPoundsRoundedFormatted}"))
          )
        )
      } else {
        Seq(Seq(
          TableRow(classes = "govuk-!-font-weight-bold", content = Text(messages("payment-complete.passengers.summary-list.amount-paid"))),
          TableRow(content = Text({
            val total: String = "£%,1.2f".format(journeySpecificData.items.map(pngrItem => pngrItem.costInGbp.toDouble).sum)
            total
          }))
        ))
      }
    }

    val normalRows: Seq[Seq[TableRow]] = Seq(
      Seq(
        TableRow(classes = "govuk-!-font-weight-bold", content = Text(messages("payment-complete.passengers.summary-list.name"))),
        TableRow(content = Text(journeySpecificData.passengerName))
      ),
      Seq(
        TableRow(classes = "govuk-!-font-weight-bold", content = Text(messages("payment-complete.passengers.summary-list.date-of-payment"))),
        TableRow(content = Text(DateStringBuilder.getDateAsString(LocalDateTime.now(clock))))
      ),
      Seq(
        TableRow(classes = "govuk-!-font-weight-bold", content = Text(messages("payment-complete.passengers.summary-list.place-of-arrival-in-uk"))),
        TableRow(content = Text(journeySpecificData.placeOfArrival))
      ),
      Seq(
        TableRow(classes = "govuk-!-font-weight-bold", content = Text(messages("payment-complete.passengers.summary-list.date-of-arrival"))),
        TableRow(content = Text(DateStringBuilder.getDateAsString(journeySpecificData.dateOfArrival)))
      ),
      Seq(
        TableRow(classes = "govuk-!-font-weight-bold", content = Text(messages("payment-complete.passengers.summary-list.time-of-arrival"))),
        TableRow(content = Text(journeySpecificData.dateOfArrival.format(DateTimeFormatter.ofPattern("hh:mm a"))))
      ),
      Seq(
        TableRow(classes = "govuk-!-font-weight-bold", content = Text(messages("payment-complete.passengers.summary-list.reference-number"))),
        TableRow(content = Text(journeySpecificData.chargeReference.value))
      )
    )

    normalRows ++ amountsTableRows
  }

  private def buildPassengersItemsDeclaredTable(journeySpecificData: JsdBcPngr)(implicit messages: Messages): Seq[Seq[TableRow]] = {
    val leadingRow = Seq(Seq(
      TableRow(classes = "govuk-!-font-weight-bold", content = Text(Messages("payment-complete.passengers.items-declared.table.item"))),
      TableRow(classes = "govuk-!-font-weight-bold", content = Text(Messages("payment-complete.passengers.items-declared.table.price"))),
      TableRow(classes = "govuk-!-font-weight-bold", content = Text(Messages("payment-complete.passengers.items-declared.table.purchased-in"))),
      TableRow(classes = "govuk-!-font-weight-bold", content = Text(Messages("payment-complete.passengers.items-declared.table.tax-paid")))
    ))
    val totalRow = Seq(Seq(
      TableRow(classes = "govuk-!-font-weight-bold", content = Text(Messages("payment-complete.passengers.items-declared.table.total"))),
      TableRow(),
      TableRow(),
      TableRow(classes = "govuk-!-font-weight-bold", content = Text("£%,1.2f".format(journeySpecificData.items.map(pngrItem => pngrItem.costInGbp.toDouble).sum)))
    ))
    val amountPrevious: AmountInPence = journeySpecificData.amountPaidPreviously.map((a: AmountPaidPreviously) => AmountInPence((a.amountPaidPreviously.toDouble * 100).toLong)).getOrElse(AmountInPence.zero)
    val maybeAmountPaidPreviouslyRow = {
      if (amountPrevious > AmountInPence.zero) {
        Seq(
          Seq(
            TableRow(content = Text(messages("payment-complete.passengers.items-declared.table.amount-paid-previously")), colspan = Some(2)),
            TableRow(),
            TableRow(content = Text(s"£${amountPrevious.inPoundsRoundedFormatted}"))
          ),
          Seq(
            TableRow(classes = "govuk-!-font-weight-bold", content = Text(messages("payment-complete.passengers.items-declared.table.total-paid-now")), colspan = Some(2)),
            TableRow(),
            TableRow(classes = "govuk-!-font-weight-bold", content = Text(s"£${journeySpecificData.totalPaidNow.map(a => AmountInPence((a.taxPaidNow.toDouble * 100).toLong)).getOrElse(AmountInPence.zero).inPoundsRoundedFormatted}"))
          )
        )
      } else Seq.empty
    }
    val itemRows: Seq[Seq[TableRow]] = journeySpecificData.items.map { item =>
      Seq(
        TableRow(content = Text(item.name)),
        TableRow(content = Text(item.price)),
        TableRow(content = Text(item.purchaseLocation)),
        TableRow(content = Text(s"£${item.costInGbp}"))
      )
    }

    leadingRow ++ itemRows ++ totalRow ++ maybeAmountPaidPreviouslyRow
  }

  private def buildPassengersTaxBreakdown(journeySpecificData: JsdBcPngr)(implicit messages: Messages): Seq[Seq[TableRow]] = Seq(
    Seq(
      TableRow(classes = "govuk-!-font-weight-bold", content = Text(Messages("payment-complete.passengers.payment-breakdown.table.type-of-tax"))),
      TableRow(classes = "govuk-!-font-weight-bold", content = Text(Messages("payment-complete.passengers.payment-breakdown.table.amount-paid")))
    ),
    Seq(
      TableRow(content = Text(Messages("payment-complete.passengers.payment-breakdown.table.customs"))),
      TableRow(content = Text(Messages(s"£%,1.2f".format(journeySpecificData.taxBreakdown.customsInGbp.toDouble))))
    ),
    Seq(
      TableRow(content = Text(Messages("payment-complete.passengers.payment-breakdown.table.excise"))),
      TableRow(content = Text(Messages(s"£%,1.2f".format(journeySpecificData.taxBreakdown.exciseInGbp.toDouble))))
    ),
    Seq(
      TableRow(content = Text(Messages("payment-complete.passengers.payment-breakdown.table.vat"))),
      TableRow(content = Text(Messages(s"£%,1.2f".format(journeySpecificData.taxBreakdown.vatInGbp.toDouble))))
    ),
    Seq(
      TableRow(classes = "govuk-!-font-weight-bold", content = Text(Messages("payment-complete.passengers.payment-breakdown.table.total"))),
      TableRow(classes = "govuk-!-font-weight-bold", content = Text(Messages("£%,1.2f".format(journeySpecificData.taxBreakdown.customsInGbp.toDouble + journeySpecificData.taxBreakdown.exciseInGbp.toDouble + journeySpecificData.taxBreakdown.vatInGbp.toDouble))))
    )
  )

  private def determineTaxAccountUrl(journey: Journey[_])(appConfig: AppConfig): Option[String] = {
    if (journey.origin.isAWebChatOrigin) {
      Some(appConfig.businessTaxAccountUrl)
    } else journey.navigation.flatMap(_.returnUrl.map(_.value))
  }

}
