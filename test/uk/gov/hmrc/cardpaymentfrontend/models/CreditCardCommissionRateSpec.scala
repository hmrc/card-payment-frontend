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

package uk.gov.hmrc.cardpaymentfrontend.models

import payapi.corcommon.model.AmountInPence
import uk.gov.hmrc.cardpaymentfrontend.testsupport.UnitSpec

class CreditCardCommissionRateSpec extends UnitSpec {

  "creditCardCommissionRate" - {
    "should be 10.0(%) for amount £100 and £10 commission" in {
      uk.gov.hmrc.cardpaymentfrontend.models.creditCardCommissionRate(AmountInPence(10000), AmountInPence(1000)) shouldBe BigDecimal(10.0)
    }
    "should be 10.05(%) for amount £100 and £10.05 commission" in {
      uk.gov.hmrc.cardpaymentfrontend.models.creditCardCommissionRate(AmountInPence(10000), AmountInPence(1005)) shouldBe BigDecimal(10.05)
    }
    "should be 10.01(%) for amount £100 and £10.01 commission" in {
      uk.gov.hmrc.cardpaymentfrontend.models.creditCardCommissionRate(AmountInPence(10000), AmountInPence(1001)) shouldBe BigDecimal(10.01)
    }
    "should be 0.001(%) for amount £1000 and £0.01 commission" in {
      uk.gov.hmrc.cardpaymentfrontend.models.creditCardCommissionRate(AmountInPence(100000), AmountInPence(1)) shouldBe BigDecimal(0.001)
    }
    "should be 0 for amount £10000 and £0.01 commission due to scale of 3 decimal places" in {
      uk.gov.hmrc.cardpaymentfrontend.models.creditCardCommissionRate(AmountInPence(1000000), AmountInPence(1)) shouldBe BigDecimal(0.0)
    }
    "should be 0.001 for amount £10000 and £0.05 commission due to rounding up" in {
      uk.gov.hmrc.cardpaymentfrontend.models.creditCardCommissionRate(AmountInPence(1000000), AmountInPence(5)) shouldBe BigDecimal(0.001)
    }
  }
}
