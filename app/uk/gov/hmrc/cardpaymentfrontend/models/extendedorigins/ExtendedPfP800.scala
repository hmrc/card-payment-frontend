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
import play.api.mvc.AnyContent
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import payapi.cardpaymentjourney.model.journey.JourneySpecificData
import uk.gov.hmrc.cardpaymentfrontend.models.CheckYourAnswersRow
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking.OriginSpecificSessionData
import uk.gov.hmrc.cardpaymentfrontend.utils._

class ExtendedPfP800 extends ExtendedOrigin {
  override val serviceNameMessageKey: String = "add.message.key.here"
  override val taxNameMessageKey: String = "payment-complete.tax-name.PfP800"
  def cardFeesPagePaymentMethods: Set[PaymentMethod] = Set.empty[PaymentMethod]
  def paymentMethods(): Set[PaymentMethod] = Set() //Set(Card, Bacs)//todo will we use this?

  def checkYourAnswersRows(request: JourneyRequest[AnyContent])(implicit messages: Messages): Seq[CheckYourAnswersRow] = Seq.empty

  override def openBankingOriginSpecificSessionData: JourneySpecificData => Option[OriginSpecificSessionData] = _ => None

  override def surveyAuditName: String = "p800-or-pa302"
  override def surveyReturnHref: String = "https://www.gov.uk/government/organisations/hm-revenue-customs"
  override def surveyReturnMessageKey: String = "payments-survey.other.return-message"
  override def surveyIsWelshSupported: Boolean = true
  override def surveyBannerTitle: String = serviceNameMessageKey

  override def emailTaxTypeMessageKey: String = "mike_add_me" //TOOD: Mike: update

}
