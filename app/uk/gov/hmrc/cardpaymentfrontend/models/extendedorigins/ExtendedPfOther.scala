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

import payapi.cardpaymentjourney.model.journey.{JourneySpecificData, JsdPfOther}
import play.api.mvc.AnyContent
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.PaymentMethod
import uk.gov.hmrc.cardpaymentfrontend.models.PaymentMethod.Card
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking.{OriginSpecificSessionData, PfOtherSessionData}

object ExtendedPfOther extends ExtendedOrigin {
  override val serviceNameMessageKey: String = "service-name.PfOther"
  override val taxNameMessageKey: String = "payment-complete.tax-name.PfOther"

  def cardFeesPagePaymentMethods: Set[PaymentMethod] = Set.empty //TODO: Probs cause an issue

  def paymentMethods(): Set[PaymentMethod] = Set(Card)

  override def checkYourAnswersReferenceRow(journeyRequest: JourneyRequest[AnyContent])
    (payFrontendBaseUrl: String): Option[uk.gov.hmrc.cardpaymentfrontend.models.CheckYourAnswersRow] = {
    Some(uk.gov.hmrc.cardpaymentfrontend.models.CheckYourAnswersRow(
      titleMessageKey = "check-your-details.PfOther.reference",
      value           = Seq(journeyRequest.journey.referenceValue),
      changeLink      = None
    ))
  }

  override def openBankingOriginSpecificSessionData: JourneySpecificData => Option[OriginSpecificSessionData] = {
    case j: JsdPfOther => j.otherRef.map(prn => PfOtherSessionData(prn))
    case _             => throw new RuntimeException("Incorrect origin found")
  }

  override def emailTaxTypeMessageKey: String = "email.tax-name.PfOther"
  override def surveyAuditName: String = "other"
  override def surveyReturnHref: String = "https://www.gov.uk/government/organisations/hm-revenue-customs"
  override def surveyReturnMessageKey: String = "payments-survey.other.return-message"
  override def surveyIsWelshSupported: Boolean = true
  override def surveyBannerTitle: String = serviceNameMessageKey

}
