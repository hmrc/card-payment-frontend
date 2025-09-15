/*
 * Copyright 2025 HM Revenue & Customs
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

import payapi.cardpaymentjourney.model.journey.{JourneySpecificData, JsdWcEpayeLateCis}
import play.api.mvc.AnyContent
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.PaymentMethod.{Card, OpenBanking}
import uk.gov.hmrc.cardpaymentfrontend.models._
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking.{OriginSpecificSessionData, WcEpayeLateCisSessionData}

object ExtendedWcEpayeLateCis extends ExtendedOrigin {
  override def serviceNameMessageKey: String = "service-name.WcEpayeLateCis"

  override def taxNameMessageKey: String = "payment-complete.tax-name.WcEpayeLateCis"

  override def cardFeesPagePaymentMethods: Set[PaymentMethod] = Set(OpenBanking)

  override def paymentMethods(): Set[PaymentMethod] = Set(Card, OpenBanking)

  override def checkYourAnswersReferenceRow(journeyRequest: JourneyRequest[AnyContent])
    (payFrontendBaseUrl: String): Option[CheckYourAnswersRow] =
    Some(CheckYourAnswersRow(
      titleMessageKey = "check-your-details.WcEpayeLateCis.reference",
      value           = Seq(journeyRequest.journey.referenceValue),
      changeLink      = None
    ))

  override def checkYourAnswersAmountSummaryRow(journeyRequest: JourneyRequest[AnyContent])(payFrontendBaseUrl: String): Option[CheckYourAnswersRow] = Some(CheckYourAnswersRow(
    titleMessageKey = "check-your-details.total-to-pay",
    value           = Seq(amount(journeyRequest)),
    changeLink      = None
  ))

  override def openBankingOriginSpecificSessionData: JourneySpecificData => Option[OriginSpecificSessionData] = {
    case j: JsdWcEpayeLateCis => Some(WcEpayeLateCisSessionData(j.chargeReference))
    case _                    => throw new RuntimeException("Incorrect origin found")
  }

  override def emailTaxTypeMessageKey: String = "email.tax-name.WcEpayeLateCis"

  override def surveyAuditName: String = "cis-late-filing-penalty"

  override def surveyReturnHref: String = "https://www.gov.uk/government/organisations/hm-revenue-customs"

  override def surveyReturnMessageKey: String = "payments-survey.other.return-message"

  override def surveyIsWelshSupported: Boolean = true

  override def surveyBannerTitle: String = serviceNameMessageKey
}
