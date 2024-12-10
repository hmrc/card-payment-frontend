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
import play.api.i18n.Messages
import play.api.mvc.{AnyContent, Call}
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking.{OriginSpecificSessionData, PtaSaSessionData}
import uk.gov.hmrc.cardpaymentfrontend.models.{Address, CheckYourAnswersRow, EmailAddress, Link}
import uk.gov.hmrc.cardpaymentfrontend.session.JourneySessionSupport._
import uk.gov.hmrc.cardpaymentfrontend.utils.PaymentMethod
import uk.gov.hmrc.cardpaymentfrontend.utils.PaymentMethods.{OneOffDirectDebit, OpenBanking}

object ExtendedPtaSa extends ExtendedOrigin {
  override val serviceNameMessageKey: String = "service-name.PtaSa"
  override val taxNameMessageKey: String = "payment-complete.tax-name.PtaSa"
  def cardFeesPagePaymentMethods: Set[PaymentMethod] = Set(OpenBanking, OneOffDirectDebit)

  //todo add these when we do that ticket
  def paymentMethods(): Set[PaymentMethod] = Set.empty

  def checkYourAnswersRows(request: JourneyRequest[AnyContent])(implicit messages: Messages): Seq[CheckYourAnswersRow] = {

    val maybeEmailAddress: Option[EmailAddress] = request.readFromSession[EmailAddress](request.journeyId, Keys.email)
    val maybeAddress: Option[Address] = request.readFromSession[Address](request.journeyId, Keys.address)

    val referenceRow =
      CheckYourAnswersRow(
        "ptasa.reference.title",
        Seq(reference(request).dropRight(1)), //Do not display the final K in the Utr in the CYA table),
        None
      )

    val dateRow = CheckYourAnswersRow(
      "ptasa.date.title",
      Seq(Messages("ptasa.date.today")),
      Some(Link(
        Call("GET", "this/that"),
        "ptasa.date-change-link",
        "ptasa.date.change-link.text"
      ))
    )

    val amountRow = CheckYourAnswersRow(
      "ptasa.amount.title",
      Seq(amount(request)),
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

    val emailRow = maybeEmailAddress match {
      case Some(emailAddress) =>
        CheckYourAnswersRow(
          titleMessageKey = "ptasa.email.title",
          value           = Seq(emailAddress.value),
          changeLink      = Some(Link(
            Call("GET", "change/email"),
            "ptasa-email-supply-link",
            "ptasa.email.supply-link.text.change"
          ))
        )
      case None =>
        CheckYourAnswersRow(
          titleMessageKey = "ptasa.email.title",
          value           = Seq.empty,
          changeLink      = Some(Link(
            Call("GET", "change/email"),
            "ptasa-email-supply-link",
            "ptasa.email.supply-link.text.new"
          ))
        )
    }

    Seq(referenceRow, dateRow, amountRow, addressRow, emailRow)
  }

  override def checkYourAnswersReferenceRow(journeyRequest: JourneyRequest[AnyContent]): Option[CheckYourAnswersRow] = {
    Some(CheckYourAnswersRow(
      titleMessageKey = "check-your-details.PtaSa.reference",
      value           = Seq(journeyRequest.journey.referenceValue),
      changeLink      = None
    ))
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

  override def emailTaxTypeMessageKey: String = "email.tax-name.PtaSa"

}
