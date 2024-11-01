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

import payapi.cardpaymentjourney.model.journey.{JourneySpecificData, JsdItSa}
import play.api.mvc.AnyContent
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.CheckYourAnswersRow
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking.{ItSaSessionData, OriginSpecificSessionData}
import uk.gov.hmrc.cardpaymentfrontend.utils.PaymentMethods.Bacs
import uk.gov.hmrc.cardpaymentfrontend.utils._
import uk.gov.hmrc.cardpaymentfrontend.utils.PaymentMethod

object ExtendedItSa extends ExtendedOrigin {
  override val serviceNameMessageKey: String = "service-name.ItSa"
  override val taxNameMessageKey: String = "payment-complete.tax-name.ItSa"
  def reference(request: JourneyRequest[AnyContent]): String = "1097172564" //This would really come from the journey either pay-api or stored locally
  def cardFeesPagePaymentMethods: Set[PaymentMethod] = Set(Bacs)
  //todo add these when we do that ticket
  def paymentMethods(): Set[PaymentMethod] = Set.empty
  //todo add this when we do that ticket
  def checkYourAnswersRows(request: JourneyRequest[AnyContent]): Seq[CheckYourAnswersRow] = Seq.empty

  override def openBankingOriginSpecificSessionData: JourneySpecificData => Option[OriginSpecificSessionData] = {
    case j: JsdItSa => Some(ItSaSessionData(j.utr))
    case _          => throw new RuntimeException("Incorrect origin found")
  }

  override def surveyAuditName: String = "self-assessment"
  override def surveyReturnHref: String = "https://www.gov.uk/government/organisations/hm-revenue-customs"
  override def surveyReturnMessageKey: String = "payments-survey.other.return-message"
  override def surveyIsWelshSupported: Boolean = true
  override def surveyBannerTitle: String = serviceNameMessageKey
}
