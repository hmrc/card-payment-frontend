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
  def reference(): String = "ma000003AP3022016" //This would really come from the journey either pay-api or stored locally
  def paymentMethods(): Set[PaymentMethod] = Set(Card(), Bacs())

  def checkYourAnswersRows(): Seq[CheckYourAnswersRow] = {
    Seq.empty
  }
}
