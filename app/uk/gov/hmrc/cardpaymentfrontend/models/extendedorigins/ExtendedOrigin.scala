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

import payapi.corcommon.model.Reference
import payapi.cardpaymentjourney.model.journey.JourneySpecificData
import play.api.i18n.Messages
import play.api.mvc.{AnyContent, Call}
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.{Address, CheckYourAnswersRow, EmailAddress, Link}
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking.OriginSpecificSessionData
import uk.gov.hmrc.cardpaymentfrontend.utils.PaymentMethod
import uk.gov.hmrc.cardpaymentfrontend.session.JourneySessionSupport._

trait ExtendedOrigin {

  def serviceNameMessageKey: String
  def taxNameMessageKey: String

  def amount(request: JourneyRequest[AnyContent]): String = s"£${request.journey.getAmountInPence.inPoundsRoundedFormatted}"

  def reference(request: JourneyRequest[AnyContent]): String = {
    request.journey.journeySpecificData.reference match {
      case Some(Reference(ref)) => ref
      case None                 => "???" //todo:... and log
    }
  }

  //denotes which links/payment methods to show on the card-fees page.
  def cardFeesPagePaymentMethods: Set[PaymentMethod]
  def paymentMethods(): Set[PaymentMethod]

  //check your answers summary rows
  def checkYourAnswersReferenceRow(journeyRequest: JourneyRequest[AnyContent]): Option[CheckYourAnswersRow]

  def checkYourAnswersAmountSummaryRow(journeyRequest: JourneyRequest[AnyContent]): Option[CheckYourAnswersRow] = Some(CheckYourAnswersRow(
    titleMessageKey = "check-your-answers.total-to-pay",
    value           = Seq(amount(journeyRequest)),
    changeLink      = Some(Link(
      href       = Call("GET", "some-link-to-pay-frontend"),
      linkId     = "check-your-answers-amount-change-link",
      messageKey = "check-your-answers.change"
    ))
  ))

  def checkYourAnswersEmailAddressRow(journeyRequest: JourneyRequest[AnyContent]): Option[CheckYourAnswersRow] = {
    val maybeEmail: Option[EmailAddress] = journeyRequest.readFromSession[EmailAddress](journeyRequest.journeyId, Keys.email)
    maybeEmail.map { email =>
      CheckYourAnswersRow(
        titleMessageKey = "check-your-answers.email-address",
        value           = Seq(email.value),
        changeLink      = Some(Link(
          href       = Call("GET", "some-link-to-address-page-on-card-payment-frontend"),
          linkId     = "check-your-answers-email-address-change-link",
          messageKey = "check-your-answers.change"
        ))
      )
    }
  }

  def checkYourAnswersCardBillingAddressRow(journeyRequest: JourneyRequest[AnyContent]): Option[CheckYourAnswersRow] = {
    //todo error? we can't take a card payment without an address
    val addressFromSession: Address = journeyRequest.readFromSession[Address](journeyRequest.journeyId, Keys.address).getOrElse(throw new RuntimeException("Cannot take a card payment without an address"))
    val addressValues: Seq[String] = Seq[String](
      addressFromSession.line1,
      addressFromSession.line2.getOrElse(""),
      addressFromSession.city.getOrElse(""),
      addressFromSession.county.getOrElse(""),
      addressFromSession.postcode,
      addressFromSession.country
    ).filter(_.nonEmpty)

    Some(CheckYourAnswersRow(
      titleMessageKey = "check-your-answers.card-billing-address",
      value           = addressValues,
      changeLink      = Some(Link(
        href       = Call("GET", "some-link-to-address-page-on-card-payment-frontend"),
        linkId     = "check-your-answers-card-billing-address-change-link",
        messageKey = "check-your-answers.change"
      ))
    ))
  }

  //todo rename, it's not quite right -- or delete when not used anymore.
  def checkYourAnswersRows(request: JourneyRequest[AnyContent])(implicit messages: Messages): Seq[CheckYourAnswersRow]
  def openBankingOriginSpecificSessionData: JourneySpecificData => Option[OriginSpecificSessionData]

  //payments survey stuff
  def surveyAuditName: String
  def surveyReturnHref: String
  def surveyReturnMessageKey: String
  def surveyIsWelshSupported: Boolean
  def surveyBannerTitle: String

}
