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

package uk.gov.hmrc.cardpaymentfrontend.models.extendedorigins

import payapi.cardpaymentjourney.model.journey.{JourneySpecificData, JsdPfVat}
import play.api.i18n.Messages
import play.api.mvc.{AnyContent, Call}
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking.{OriginSpecificSessionData, PfVatSessionData}
import uk.gov.hmrc.cardpaymentfrontend.models.{CheckYourAnswersRow, Link}
import uk.gov.hmrc.cardpaymentfrontend.utils.PaymentMethods._
import uk.gov.hmrc.cardpaymentfrontend.utils._

object ExtendedPfVat extends ExtendedOrigin {
  override val serviceNameMessageKey: String = "add.message.key.here"
  override val taxNameMessageKey: String = "payment-complete.tax-name.PfVat"
  def cardFeesPagePaymentMethods: Set[PaymentMethod] = Set.empty[PaymentMethod]
  def paymentMethods(): Set[PaymentMethod] = Set(Card, OpenBanking, VariableDirectDebit, Bacs)

  def checkYourAnswersRows(request: JourneyRequest[AnyContent])(implicit messages: Messages): Seq[CheckYourAnswersRow] = {
    val referenceRow =
      CheckYourAnswersRow(
        "pfvat.reference.title",
        Seq(reference(request)), //This would really come from the journey either pay-api or stored locally
        Some(Link(
          Call("GET", "this/that"),
          "pfvat-reference-change-link",
          "pfvat.reference.change-link.text"
        ))
      )

    val amountRow = CheckYourAnswersRow(
      "pfvat.amount.title",
      Seq("£600"),
      Some(Link(
        Call("GET", "this/that"),
        "pfvat-amount-change-link",
        "pfvat.amount.change-link.text"
      ))
    )

    val addressRow = CheckYourAnswersRow(
      "pfsa.address.title",
      Seq("24 Andrews Close", "Warnington", "West Sussex", "BN11 7PG"), //This would really come from the journey either pay-api or stored locally
      Some(Link(
        Call("GET", "this/that"),
        "pfsa-address-change-link",
        "pfsa.address.change-link.text"
      ))
    )

    val emailRow = CheckYourAnswersRow(
      "pfsa.email.title",
      Seq("fdobbs1972@gmail.com"),
      Some(Link(
        Call("GET", "change/email"),
        "pfvat-email-change-link",
        "pfvat.email.change-link.text"
      ))
    )
    Seq(referenceRow, amountRow, addressRow, emailRow)
  }

  override def checkYourAnswersReferenceRow(journeyRequest: JourneyRequest[AnyContent]): Option[CheckYourAnswersRow] = None

  override def openBankingOriginSpecificSessionData: JourneySpecificData => Option[OriginSpecificSessionData] = {
    case j: JsdPfVat => Some(PfVatSessionData(j.vrn, j.chargeRef))
    case _           => throw new RuntimeException("Incorrect origin found")
  }

  override def surveyAuditName: String = "vat"
  override def surveyReturnHref: String = "https://www.gov.uk/government/organisations/hm-revenue-customs"
  override def surveyReturnMessageKey: String = "payments-survey.other.return-message"
  override def surveyIsWelshSupported: Boolean = true
  override def surveyBannerTitle: String = serviceNameMessageKey

  override def emailTaxTypeMessageKey: String = "email.tax-name.PfVat"
}
