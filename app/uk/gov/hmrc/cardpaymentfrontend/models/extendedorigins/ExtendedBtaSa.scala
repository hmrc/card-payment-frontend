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
import uk.gov.hmrc.cardpaymentfrontend.models.{Address, CheckYourAnswersRow, EmailAddress, Link}
import uk.gov.hmrc.cardpaymentfrontend.utils._
import uk.gov.hmrc.cardpaymentfrontend.session.JourneySessionSupport._

object ExtendedBtaSa extends ExtendedOrigin {
  override val serviceNameMessageKey: String = "service-name.BtaSa"
  override val taxNameMessageKey: String = "payment-complete.tax-name.BtaSa"

  //todo add these when we do that ticket
  def paymentMethods(): Set[PaymentMethod] = Set.empty
  //todo add this when we do that ticket

  private def referenceRow(request: JourneyRequest[AnyContent]) = CheckYourAnswersRow(
    "btasa.reference.title",
    Seq(reference(request)),
    Some(Link(
      Call("GET", "this/that"),
      "pfvat-reference-change-link",
      "pfvat.reference.change-link.text"
    ))
  )

  private def amountRow(request: JourneyRequest[AnyContent]) = CheckYourAnswersRow(
    "btasa.amount.title",
    Seq(amount(request)),
    Some(Link(
      Call("GET", "this/that"),
      "pfvat-reference-change-link",
      "pfvat.reference.change-link.text"
    ))
  )

  private def addressRow(request: JourneyRequest[AnyContent]) = {
    val maybeAddress: Option[Address] = request.readFromSession[Address](request.journeyId, Keys.address)
    CheckYourAnswersRow(
      "btasa.address.title",
      maybeAddress match {
        case Some(addr) => Seq(addr.line1, addr.line2.getOrElse(""), addr.city.getOrElse(""), addr.county.getOrElse(""), addr.postcode, addr.country).filter(_.nonEmpty)
        case None       => Seq.empty
      },
      Some(Link(
        Call("GET", "this/that"),
        "ptasa-address-change-link",
        "ptasa.address.change-link.text"
      ))
    )
  }

  private def emailRow(request: JourneyRequest[AnyContent]) = {
    val maybeEmailAddress: Option[EmailAddress] = request.readFromSession[EmailAddress](request.journeyId, Keys.email)

    CheckYourAnswersRow(
      "btasa.email.title",
      maybeEmailAddress match {
        case Some(emailAddress) => Seq(emailAddress.value)
        case None               => Seq.empty
      },
      Some(Link(
        Call("GET", "change/email"),
        "btasa-email-supply-link",
        "btasa.email.supply-link.text"
      ))
    )
  }

  def checkYourAnswersRows(request: JourneyRequest[AnyContent]): Seq[CheckYourAnswersRow] =
    Seq(
      referenceRow(request),
      amountRow(request),
      addressRow(request),
      emailRow(request)
    )

}
