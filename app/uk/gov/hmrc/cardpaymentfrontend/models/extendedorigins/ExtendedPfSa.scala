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

import payapi.cardpaymentjourney.model.journey.{JourneySpecificData, JsdPfSa}
import play.api.i18n.Messages
import play.api.mvc.{AnyContent, Call}
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking.{OriginSpecificSessionData, PfSaSessionData}
import uk.gov.hmrc.cardpaymentfrontend.models.{Address, CheckYourAnswersRow, EmailAddress, Link}
import uk.gov.hmrc.cardpaymentfrontend.session.JourneySessionSupport._
import uk.gov.hmrc.cardpaymentfrontend.utils.PaymentMethod
import uk.gov.hmrc.cardpaymentfrontend.utils.PaymentMethods.{Bacs, Card, OneOffDirectDebit, OpenBanking}

object ExtendedPfSa extends ExtendedOrigin {
  override val serviceNameMessageKey: String = "service-name.PfSa"
  override val taxNameMessageKey: String = "payment-complete.tax-name.PfSa"

  def cardFeesPagePaymentMethods: Set[PaymentMethod] = Set(OpenBanking, OneOffDirectDebit)

  def paymentMethods(): Set[PaymentMethod] = Set(Card, OpenBanking, OneOffDirectDebit, Bacs)

  def checkYourAnswersRows(request: JourneyRequest[AnyContent])(implicit messages: Messages): Seq[CheckYourAnswersRow] = {

    val maybeEmailAddress: Option[EmailAddress] = request.readFromSession[EmailAddress](request.journeyId, Keys.email)
    val maybeAddress: Option[Address] = request.readFromSession[Address](request.journeyId, Keys.address)

    val referenceRow =
      CheckYourAnswersRow(
        titleMessageKey = "PfSa.reference.title",
        value           = Seq(reference(request).dropRight(1)), //Do not display the final K in the Utr in the CYA table//TODO: We should not drop right. We should use the dedicated methods to display sa utr.
        changeLink      = Some(Link(
          Call("GET", "this/that"),
          linkId     = "PfSa-reference-change-link",
          messageKey = "PfSa.reference.change-link.text"
        ))
      )

    val amountRow = CheckYourAnswersRow(
      titleMessageKey = "PfSa.amount.title",
      value           = Seq(amount(request)),
      changeLink      = Some(Link(
        Call("GET", "this/that"),
        linkId     = "PfSa-amount-change-link",
        messageKey = "PfSa.amount.change-link.text"
      ))
    )

    val addressRow = CheckYourAnswersRow(
      titleMessageKey = "PfSa.address.title",
      value           = maybeAddress match {
        case Some(addr) => Seq(addr.line1, addr.line2.getOrElse(""), addr.city.getOrElse(""), addr.county.getOrElse(""), addr.postcode, addr.country).filter(_.nonEmpty)
        case None       => Seq.empty
      },
      changeLink      = Some(Link(
        Call("GET", "this/that"),
        linkId     = "PfSa-address-change-link",
        messageKey = "PfSa.address.change-link.text"
      ))
    )

    val emailRow = maybeEmailAddress match {
      case Some(emailAddress) =>
        CheckYourAnswersRow(
          titleMessageKey = "PfSa.email.title",
          value           = Seq(emailAddress.value),
          changeLink      = Some(Link(
            Call("GET", "change/email"),
            linkId     = "PfSa-email-supply-link",
            messageKey = "PfSa.email.supply-link.text.change"
          ))
        )
      case None =>
        CheckYourAnswersRow(
          titleMessageKey = "PfSa.email.title",
          value           = Seq.empty,
          changeLink      = Some(Link(
            Call("GET", "change/email"),
            linkId     = "PfSa-email-supply-link",
            messageKey = "PfSa.email.supply-link.text.new"
          ))
        )
    }

    Seq(referenceRow, amountRow, addressRow, emailRow)
  }

  override def checkYourAnswersReferenceRow(journeyRequest: JourneyRequest[AnyContent]): Option[CheckYourAnswersRow] = {
    Some(CheckYourAnswersRow(
      titleMessageKey = "check-your-details.PfSa.reference",
      value           = Seq(journeyRequest.journey.referenceValue),
      changeLink      = Some(Link(
        href       = Call("GET", "some-link-to-pay-frontend"),
        linkId     = "check-your-details-reference-change-link",
        messageKey = "check-your-details.change"
      ))
    ))
  }

  override def openBankingOriginSpecificSessionData: JourneySpecificData => Option[OriginSpecificSessionData] = {
    case j: JsdPfSa => j.utr.map(PfSaSessionData(_))
    case _          => throw new RuntimeException("Incorrect origin found")
  }

  override def emailTaxTypeMessageKey: String = "email.tax-name.PfSa"

  override def surveyAuditName: String = "self-assessment"
  override def surveyReturnHref: String = "https://www.gov.uk/government/organisations/hm-revenue-customs"
  override def surveyReturnMessageKey: String = "payments-survey.other.return-message"
  override def surveyIsWelshSupported: Boolean = true
  override def surveyBannerTitle: String = serviceNameMessageKey
}
