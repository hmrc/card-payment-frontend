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

import play.api.mvc.Call
import uk.gov.hmrc.cardpaymentfrontend.models.{CheckYourAnswersRow, Link}
import uk.gov.hmrc.cardpaymentfrontend.utils.{Bacs, Card, OneOffDirectDebit, OpenBanking, PaymentMethod}

object ExtendedPfSa extends ExtendedOrigin {
  override val serviceNameMessageKey: String = "service-name.PfSa"
  override val taxNameMessageKey: String = "payment-complete.tax-name.PfSa"
  def reference(): String = "1097172564" //This would really come from the journey either pay-api or stored locally
  def paymentMethods(): Set[PaymentMethod] = Set(Card, OpenBanking, OneOffDirectDebit, Bacs)

  def checkYourAnswersRows(): Seq[CheckYourAnswersRow] = {
    val referenceRow =
      CheckYourAnswersRow(
        "pfsa.reference.title",
        Seq(reference()),
        Some(Link(
          Call("GET", "this/that"),
          "pfsa-reference-change-link",
          "pfsa.reference.change-link.text"
        ))
      )

    val amountRow = CheckYourAnswersRow(
      "pfsa.amount.title",
      Seq("£600"),
      Some(Link(
        Call("GET", "this/that"),
        "pfsa-amount-change-link",
        "pfsa.amount.change-link.text"
      ))
    )

    val addressRow = CheckYourAnswersRow(
      "pfsa.address.title",
      Seq("14 High St", "Beedington", "West Sussex", "RH12 0QR"), //This would really come from the journey either pay-api or stored locally
      Some(Link(
        Call("GET", "this/that"),
        "pfsa-address-change-link",
        "pfsa.address.change-link.text"
      ))
    )

    val emailRow = CheckYourAnswersRow(
      "pfsa.email.title",
      Seq.empty,
      Some(Link(
        Call("GET", "change/email"),
        "pfsa-email-supply-link",
        "pfsa.email.supply-link.text"
      ))
    )
    Seq(referenceRow, amountRow, addressRow, emailRow)
  }
}
