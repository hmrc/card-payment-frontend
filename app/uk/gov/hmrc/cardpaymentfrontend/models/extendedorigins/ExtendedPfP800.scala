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

import uk.gov.hmrc.cardpaymentfrontend.models.CheckYourAnswersRow
import uk.gov.hmrc.cardpaymentfrontend.utils._

class ExtendedPfP800 extends ExtendedOrigin {
  override val serviceNameMessageKey: String = "add.message.key.here"
  override val taxNameMessageKey: String = "payment-complete.tax-name.PfP800"
  def reference(): String = "ma000003AP3022016" //This would really come from the journey either pay-api or stored locally
  def cardFeesPagePaymentMethods: Set[PaymentMethod] = Set.empty[PaymentMethod]
  def paymentMethods(): Set[PaymentMethod] = Set() //Set(Card, Bacs)//todo will we use this?

  def checkYourAnswersRows(): Seq[CheckYourAnswersRow] = {
    Seq.empty
  }
}
