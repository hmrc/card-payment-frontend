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

import payapi.cardpaymentjourney.model.journey.{JourneySpecificData, JsdPtaSa}
import uk.gov.hmrc.cardpaymentfrontend.models.CheckYourAnswersRow
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking.{OriginSpecificSessionData, PtaSaSessionData}
import uk.gov.hmrc.cardpaymentfrontend.utils.PaymentMethods.{OneOffDirectDebit, OpenBanking}
import uk.gov.hmrc.cardpaymentfrontend.utils._
import payapi.corcommon.model.Reference
import play.api.mvc.{AnyContent, Call}
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.{Address, CheckYourAnswersRow, EmailAddress, Link}
import uk.gov.hmrc.cardpaymentfrontend.utils.PaymentMethod
import uk.gov.hmrc.cardpaymentfrontend.session.JourneySessionSupport._

object ExtendedPtaSa extends ExtendedOrigin {
  override val serviceNameMessageKey: String = "service-name.PtaSa"
  override val taxNameMessageKey: String = "payment-complete.tax-name.PtaSa"
  def cardFeesPagePaymentMethods: Set[PaymentMethod] = Set(OpenBanking, OneOffDirectDebit)

  def reference(request: JourneyRequest[AnyContent]): String = {
    request.journey.journeySpecificData.reference match {
      case Some(Reference(ref)) => ref
      case None                 => "" //todo: ...and Log
    }
  }

  //todo add these when we do that ticket
  def paymentMethods(): Set[PaymentMethod] = Set.empty

  def checkYourAnswersRows(request: JourneyRequest[AnyContent]): Seq[CheckYourAnswersRow] = {

    val maybeEmailAddress: Option[EmailAddress] = request.readFromSession[EmailAddress](request.journeyId, Keys.email)
    val maybeAddress: Option[Address] = request.readFromSession[Address](request.journeyId, Keys.address)
    val amount: String = request.journey.amountInPence match {
      case Some(amt) => amt.formatInPounds
      case None      => "" //todo: logging here
    }

    val referenceRow =
      CheckYourAnswersRow(
        "ptasa.reference.title",
        Seq(reference(request)),
        Some(Link(
          Call("GET", "this/that"),
          "ptasa-reference-change-link",
          "ptasa.reference.change-link.text"
        ))
      )

    val amountRow = CheckYourAnswersRow(
      "ptasa.amount.title",
      Seq(amount),
      Some(Link(
        Call("GET", "this/that"),
        "ptasa-amount-change-link",
        "ptasa.amount.change-link.text"
      ))
    )

    val addressRow = CheckYourAnswersRow(
      "ptasa.address.title",
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

  override def openBankingOriginSpecificSessionData: JourneySpecificData => Option[OriginSpecificSessionData] = {
    case j: JsdPtaSa => Some(PtaSaSessionData(j.utr))
    case _           => throw new RuntimeException("Incorrect origin found")
  }

  override def surveyAuditName: String = "self-assessment"
  override def surveyReturnHref: String = "/personal-account"
  override def surveyReturnMessageKey: String = "payments-survey.pta.return-message"
  override def surveyIsWelshSupported: Boolean = true
  override def surveyBannerTitle: String = serviceNameMessageKey
}
