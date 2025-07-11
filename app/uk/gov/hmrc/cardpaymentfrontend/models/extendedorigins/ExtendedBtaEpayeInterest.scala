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

import payapi.cardpaymentjourney.model.journey.{JourneySpecificData, JsdBtaEpayeInterest}
import play.api.mvc.AnyContent
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.PaymentMethod._
import uk.gov.hmrc.cardpaymentfrontend.models._
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking.{BtaEpayeInterestSessionData, OriginSpecificSessionData}

object ExtendedBtaEpayeInterest extends ExtendedOrigin {
  override val serviceNameMessageKey: String = "service-name.BtaEpayeInterest"
  override val taxNameMessageKey: String = "payment-complete.tax-name.BtaEpayeInterest"

  def cardFeesPagePaymentMethods: Set[PaymentMethod] = Set(OpenBanking, OneOffDirectDebit)
  def paymentMethods(): Set[PaymentMethod] = Set(Card, OpenBanking, OneOffDirectDebit, Bacs)

  override def checkYourAnswersReferenceRow(journeyRequest: JourneyRequest[AnyContent])(payFrontendBaseUrl: String): Option[CheckYourAnswersRow] = {
    Some(CheckYourAnswersRow(
      titleMessageKey = "check-your-details.BtaEpayeInterest.reference",
      value           = Seq(journeyRequest.journey.referenceValue),
      changeLink      = None
    ))
  }

  override def openBankingOriginSpecificSessionData: JourneySpecificData => Option[OriginSpecificSessionData] = {
    case j: JsdBtaEpayeInterest => Some(BtaEpayeInterestSessionData(j.xRef))
    case _                      => throw new RuntimeException("Incorrect origin found")
  }

  override def surveyAuditName: String = "paye-interest"
  override def surveyReturnHref: String = "/business-account"
  override def surveyReturnMessageKey: String = "payments-survey.bta.return-message"
  override def surveyIsWelshSupported: Boolean = true
  override def surveyBannerTitle: String = serviceNameMessageKey

  override def emailTaxTypeMessageKey: String = "email.tax-name.BtaEpayeInterest"
}
