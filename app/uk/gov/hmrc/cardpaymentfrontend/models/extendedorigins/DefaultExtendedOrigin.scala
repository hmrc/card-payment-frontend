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

import payapi.cardpaymentjourney.model.journey.JourneySpecificData
import uk.gov.hmrc.cardpaymentfrontend.models.CheckYourAnswersRow
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking.OriginSpecificSessionData
import uk.gov.hmrc.cardpaymentfrontend.utils.PaymentMethod

class DefaultExtendedOrigin extends ExtendedOrigin {
  def serviceNameMessageKey = ""
  def taxNameMessageKey: String = ""
  def reference(): String = ""
  def cardFeesPagePaymentMethods: Set[PaymentMethod] = Set.empty[PaymentMethod]
  def paymentMethods(): Set[PaymentMethod] = Set.empty[PaymentMethod]
  def checkYourAnswersRows(): Seq[CheckYourAnswersRow] = Seq.empty[CheckYourAnswersRow]

  override def openBankingOriginSpecificSessionData: JourneySpecificData => Option[OriginSpecificSessionData] = _ => None

  override def surveyAuditName: String = ""
  override def surveyReturnHref: String = "https://www.gov.uk/government/organisations/hm-revenue-customs"
  override def surveyReturnMessageKey: String = "payments-survey.other.return-message"
  override def surveyIsWelshSupported: Boolean = false
  override def surveyBannerTitle: String = serviceNameMessageKey
}
