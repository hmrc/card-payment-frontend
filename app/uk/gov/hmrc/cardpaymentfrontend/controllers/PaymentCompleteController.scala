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

import payapi.cardpaymentjourney.model.journey.{Journey, JourneySpecificData, JsdAlcoholDuty}
import payapi.corcommon.model.barclays.CardCategories
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import uk.gov.hmrc.cardpaymentfrontend.actions.{Actions, JourneyRequest}
import uk.gov.hmrc.cardpaymentfrontend.models.extendedorigins.ExtendedOrigin.OriginExtended
import uk.gov.hmrc.cardpaymentfrontend.models.{EmailAddress, creditCardCommissionRate}
import uk.gov.hmrc.cardpaymentfrontend.requests.RequestSupport
import uk.gov.hmrc.cardpaymentfrontend.services.EmailService
import uk.gov.hmrc.cardpaymentfrontend.session.JourneySessionSupport._
import uk.gov.hmrc.cardpaymentfrontend.util.SafeEquals.EqualsOps
import uk.gov.hmrc.cardpaymentfrontend.utils.DateStringBuilder
import uk.gov.hmrc.cardpaymentfrontend.views.html.PaymentCompletePage
import uk.gov.hmrc.govukfrontend.views.Aliases.{HtmlContent, Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PaymentCompleteController @Inject() (
    actions:             Actions,
    emailService:        EmailService,
    mcc:                 MessagesControllerComponents,
    paymentCompletePage: PaymentCompletePage,
    requestSupport:      RequestSupport
)(implicit executionContext: ExecutionContext) extends FrontendController(mcc) {

  import requestSupport._

  val renderPage: Action[AnyContent] = actions.journeyAction { implicit journeyRequest: JourneyRequest[AnyContent] =>

    val maybeEmailFromSession: Option[EmailAddress] =
      journeyRequest.readFromSession[EmailAddress](journeyRequest.journeyId, Keys.email).filter(!_.value.isBlank).map(email => EmailAddress(email.value))

    val langIsEnglish: Boolean = journeyRequest.lang.code =!= "cy"

    //TODO: Eventually we can call this from the payment-status controller, but we don't have that yet.
    val _ = maybeEmailFromSession.fold(Future.unit) { email =>
      emailService.sendEmail(
        journey      = journeyRequest.journey,
        emailAddress = email,
        isEnglish    = langIsEnglish
      )(RequestSupport.hc, journeyRequest)
    }.map((_: Unit) => ())

    Ok(paymentCompletePage(
      taxReference      = journeyRequest.journey.getReference,
      summaryListRows   = PaymentCompleteController.buildSummaryListRows(
        journey = journeyRequest.journey,
        taxType = journeyRequest.journey.origin.lift.taxNameMessageKey
      ),
      maybeEmailAddress = maybeEmailFromSession,
      maybeReturnUrl    = journeyRequest.journey.navigation.flatMap(_.returnUrl.map(_.value))
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

}
