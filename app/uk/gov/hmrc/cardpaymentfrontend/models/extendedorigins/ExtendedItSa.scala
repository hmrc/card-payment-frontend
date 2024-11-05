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

import play.api.i18n.Messages
import payapi.cardpaymentjourney.model.journey.{JourneySpecificData, JsdItSa}
import play.api.mvc.AnyContent
import play.api.mvc.{AnyContent, Call}
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.CheckYourAnswersRow
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking.{ItSaSessionData, OriginSpecificSessionData}
import uk.gov.hmrc.cardpaymentfrontend.utils.PaymentMethods.Bacs
import uk.gov.hmrc.cardpaymentfrontend.utils._
import uk.gov.hmrc.cardpaymentfrontend.models.{Address, CheckYourAnswersRow, EmailAddress, Link}
import uk.gov.hmrc.cardpaymentfrontend.utils.PaymentMethod
import uk.gov.hmrc.cardpaymentfrontend.session.JourneySessionSupport._

object ExtendedItSa extends ExtendedOrigin {
  override val serviceNameMessageKey: String = "service-name.ItSa"
  override val taxNameMessageKey: String = "payment-complete.tax-name.ItSa"
  def reference(request: JourneyRequest[AnyContent]): String = "1097172564" //This would really come from the journey either pay-api or stored locally
  def cardFeesPagePaymentMethods: Set[PaymentMethod] = Set(Bacs)
  //todo add these when we do that ticket
  def paymentMethods(): Set[PaymentMethod] = Set.empty

  override def openBankingOriginSpecificSessionData: JourneySpecificData => Option[OriginSpecificSessionData] = {
    case j: JsdItSa => Some(ItSaSessionData(j.utr))
    case _          => throw new RuntimeException("Incorrect origin found")
  }

  override def surveyAuditName: String = "self-assessment"
  override def surveyReturnHref: String = "https://www.gov.uk/government/organisations/hm-revenue-customs"
  override def surveyReturnMessageKey: String = "payments-survey.other.return-message"
  override def surveyIsWelshSupported: Boolean = true
  override def surveyBannerTitle: String = serviceNameMessageKey

  def checkYourAnswersRows(request: JourneyRequest[AnyContent])(implicit messages: Messages): Seq[CheckYourAnswersRow] = {
    val maybeEmailAddress: Option[EmailAddress] = request.readFromSession[EmailAddress](request.journeyId, Keys.email)
    val maybeAddress: Option[Address] = request.readFromSession[Address](request.journeyId, Keys.address)

    val referenceRow =
      CheckYourAnswersRow(
        "itsa.reference.title",
        Seq(reference(request)),
        Some(Link(
          Call("GET", "this/that"),
          "itsa-reference-change-link",
          "itsa.reference.change-link.text"
        ))
      )

    val dateRow = CheckYourAnswersRow(
      "itsa.date.title",
      Seq(Messages("itsa.date.today")),
      Some(Link(
        Call("GET", "this/that"),
        "itsa.date-change-link",
        "itsa.date.change-link.text"
      ))
    )

    val amountRow = CheckYourAnswersRow(
      "itsa.amount.title",
      Seq(amount(request)),
      Some(Link(
        Call("GET", "this/that"),
        "itsa-amount-change-link",
        "itsa.amount.change-link.text"
      ))
    )

    val addressRow = CheckYourAnswersRow(
      "itsa.address.title",
      maybeAddress match {
        case Some(addr) => Seq(addr.line1, addr.line2.getOrElse(""), addr.city.getOrElse(""), addr.county.getOrElse(""), addr.postcode, addr.country).filter(_.nonEmpty)
        case None       => Seq.empty
      },
      Some(Link(
        Call("GET", "this/that"),
        "itsa-address-change-link",
        "itsa.address.change-link.text"
      ))
    )

    val emailRow = maybeEmailAddress match {
      case Some(emailAddress) =>
        CheckYourAnswersRow(
          titleMessageKey = "itsa.email.title",
          value           = Seq(emailAddress.value),
          changeLink      = Some(Link(
            Call("GET", "change/email"),
            "itsa-email-supply-link",
            "itsa.email.supply-link.text.change"
          ))
        )
      case None =>
        CheckYourAnswersRow(
          titleMessageKey = "itsa.email.title",
          value           = Seq.empty,
          changeLink      = Some(Link(
            Call("GET", "change/email"),
            "itsa-email-supply-link",
            "itsa.email.supply-link.text.new"
          ))
        )
    }

    Seq(referenceRow, dateRow, amountRow, addressRow, emailRow)
  }
}
