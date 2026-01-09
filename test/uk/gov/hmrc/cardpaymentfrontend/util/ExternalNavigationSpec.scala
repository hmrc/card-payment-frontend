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

package uk.gov.hmrc.cardpaymentfrontend.util

import org.scalatest.prop.TableDrivenPropertyChecks
import payapi.cardpaymentjourney.model.journey.Url
import uk.gov.hmrc.cardpaymentfrontend.testsupport.UnitSpec
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.TestJourneys

class ExternalNavigationSpec extends UnitSpec with TableDrivenPropertyChecks {

  "returnUrlCancelled" - {

    "should return Some[Url] when navigation in journey contains a returnUrl" in {
      ExternalNavigation.returnUrlCancelled(TestJourneys.BtaSa.journeyAfterCancelWebPayment) shouldBe Some(Url("https://www.return-url.com"))
    }
    "should return None when navigation in journey is None" in {
      ExternalNavigation.returnUrlCancelled(TestJourneys.PfSa.journeyAfterCancelWebPayment) shouldBe None
    }
    "should return None when navigation in journey is Some but the returnUrlCancelled is None" in {
      ExternalNavigation.returnUrlCancelled(
        TestJourneys.BtaSa.journeyAfterCancelWebPayment
          .copy(navigation = TestJourneys.BtaSa.journeyAfterCancelWebPayment.navigation.map(_.copy(returnUrl = None)))
      ) shouldBe None
    }
  }
}
