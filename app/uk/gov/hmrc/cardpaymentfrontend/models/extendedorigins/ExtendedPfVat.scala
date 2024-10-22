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
import uk.gov.hmrc.cardpaymentfrontend.utils.{Bacs, Card, OpenBanking, PaymentMethod, VariableDirectDebit}

class ExtendedPfVat extends ExtendedOrigin {
  override val serviceNameMessageKey: String = "add.message.key.here"
  override val taxNameMessageKey: String = "payment-complete.tax-name.PfVat"
  def reference(): String = "999964805"
  def paymentMethods(): Set[PaymentMethod] = Set(Card, OpenBanking, VariableDirectDebit, Bacs)

  def checkYourAnswersRows(): Seq[CheckYourAnswersRow] = {
    val referenceRow =
      CheckYourAnswersRow(
        "pfvat.reference.title",
        Seq(reference()), //This would really come from the journey either pay-api or stored locally
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

}
