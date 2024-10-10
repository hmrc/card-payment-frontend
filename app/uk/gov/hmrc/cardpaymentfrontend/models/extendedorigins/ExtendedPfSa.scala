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

class ExtendedPfSa extends ExtendedOrigin {
  def paymentMethods(): Set[PaymentMethod] = Set(Card(), OpenBanking(), OneOffDirectDebit(), Bacs())

  def checkYourAnswersRows(): Seq[CheckYourAnswersRow] = {
    val referenceRow =
      CheckYourAnswersRow(
        "PfSa.reference.title",
        Some("1097172564"),
        Some(Link(
          Call("GET", "this/that"),
          "reference-change-link",
          "PfSa.reference.change-link.text"
        ))
      )
    val emailRow = CheckYourAnswersRow(
      "PfSa.reference.email",
      None,
      Some(Link(
        Call("GET", "change/email"),
        "reference-change-link",
        "PfSa.email.change-link.text"
      ))
    )
    Seq(referenceRow, emailRow)
  }
}
