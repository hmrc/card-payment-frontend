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

import payapi.cardpaymentjourney.model.journey.{JourneySpecificData, JsdWcClass1aNi}
import play.api.i18n.Lang
import play.api.mvc.AnyContent
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.PaymentMethod._
import uk.gov.hmrc.cardpaymentfrontend.models._
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking.{WcClass1aNiSessionData, OriginSpecificSessionData}
import uk.gov.hmrc.cardpaymentfrontend.util.Period.humanReadablePeriod

object ExtendedWcClass1aNi extends ExtendedOrigin {
  override val serviceNameMessageKey: String = "service-name.WcClass1aNi"
  override val taxNameMessageKey: String = "payment-complete.tax-name.WcClass1aNi"

  def cardFeesPagePaymentMethods: Set[PaymentMethod] = Set(OpenBanking)
  def paymentMethods(): Set[PaymentMethod] = Set(Card, OpenBanking, Bacs)

  override def checkYourAnswersReferenceRow(journeyRequest: JourneyRequest[AnyContent])(payFrontendBaseUrl: String): Option[CheckYourAnswersRow] = {
    Some(CheckYourAnswersRow(
      titleMessageKey = "check-your-details.WcClass1aNi.reference",
      value           = Seq(journeyRequest.journey.referenceValue),
      changeLink      = None
    ))
  }

  override def checkYourAnswersAdditionalReferenceRow(journeyRequest: JourneyRequest[AnyContent])
    (payFrontendBaseUrl: String)(implicit lang: Lang): Option[CheckYourAnswersRow] =
    Some(CheckYourAnswersRow(
      titleMessageKey = "check-your-details.WcClass1aNi.tax-period",
      value           = Seq(humanReadablePeriod(journeyRequest.journey.journeySpecificData.asInstanceOf[JsdWcClass1aNi].period)),
      changeLink      = None
    ))

  override def openBankingOriginSpecificSessionData: JourneySpecificData => Option[OriginSpecificSessionData] = {
    case j: JsdWcClass1aNi => Some(WcClass1aNiSessionData(j.accountsOfficeReference, period = j.period))
    case _                 => throw new RuntimeException("Incorrect origin found")
  }

  override def surveyAuditName: String = "class-1a-national-insurance"
  override def surveyReturnHref: String = "/paye"
  override def surveyReturnMessageKey: String = "payments-survey.other.return-message"
  override def surveyIsWelshSupported: Boolean = true
  override def surveyBannerTitle: String = serviceNameMessageKey

  override def emailTaxTypeMessageKey: String = "email.tax-name.WcClass1aNi"
}
