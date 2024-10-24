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

import play.api.mvc.{AnyContent, Call}
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.{CheckYourAnswersRow, EmailAddress, Link}
import uk.gov.hmrc.cardpaymentfrontend.utils.PaymentMethod
import uk.gov.hmrc.cardpaymentfrontend.session.JourneySessionSupport._

object ExtendedPtaSa extends ExtendedOrigin {
  override val serviceNameMessageKey: String = "service-name.PtaSa"
  override val taxNameMessageKey: String = "payment-complete.tax-name.PtaSa"
  def reference(): String = "1097172564" //This would really come from the journey either pay-api or stored locally
  //todo add these when we do that ticket
  def paymentMethods(): Set[PaymentMethod] = Set.empty

  def checkYourAnswersRows(request: JourneyRequest[AnyContent]): Seq[CheckYourAnswersRow] = {

    val maybeEmailAddress: Option[EmailAddress] = request.readFromSession[EmailAddress](request.journeyId, Keys.email)

    val referenceRow =
      CheckYourAnswersRow(
        "ptasa.reference.title",
        Seq(reference()),
        Some(Link(
          Call("GET", "this/that"),
          "ptasa-reference-change-link",
          "ptasa.reference.change-link.text"
        ))
      )

    val amountRow = CheckYourAnswersRow(
      "ptasa.amount.title",
      Seq("£600"),
      Some(Link(
        Call("GET", "this/that"),
        "ptasa-amount-change-link",
        "ptasa.amount.change-link.text"
      ))
    )

    val addressRow = CheckYourAnswersRow(
      "pfsa.address.title",
      Seq("14 High St", "Beedington", "West Sussex", "RH12 0QR"), //This would really come from the journey either pay-api or stored locally
      Some(Link(
        Call("GET", "this/that"),
        "ptasa-address-change-link",
        "ptasa.address.change-link.text"
      ))
    )

    val emailRow = CheckYourAnswersRow(
      "ptasa.email.title",
      maybeEmailAddress match {
        case Some(emailAddress) => Seq(emailAddress.value)
        case None               => Seq.empty
      },
      Some(Link(
        Call("GET", "change/email"),
        "ptasa-email-supply-link",
        "ptasa.email.supply-link.text"
      ))
    )

    Seq(referenceRow, amountRow, addressRow, emailRow)
  }
}
