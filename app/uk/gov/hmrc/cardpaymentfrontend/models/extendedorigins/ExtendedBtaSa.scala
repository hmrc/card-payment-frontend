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

object ExtendedBtaSa extends ExtendedOrigin {
  override val serviceNameMessageKey: String = "service-name.BtaSa"
  override val taxNameMessageKey: String = "payment-complete.tax-name.BtaSa"
  def reference(): String = "1097172564" //This would really come from the journey either pay-api or stored locally
  //todo add these when we do that ticket
  def paymentMethods(): Set[PaymentMethod] = Set.empty
  //todo add this when we do that ticket
  def checkYourAnswersRows(): Seq[CheckYourAnswersRow] = Seq.empty
}
