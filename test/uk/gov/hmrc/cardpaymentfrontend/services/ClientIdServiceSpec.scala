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

package uk.gov.hmrc.cardpaymentfrontend.services

import uk.gov.hmrc.cardpaymentfrontend.models.Languages
import uk.gov.hmrc.cardpaymentfrontend.models.cardpayment.ClientIds
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.TestJourneys
import uk.gov.hmrc.cardpaymentfrontend.testsupport.{ItSpec, TestHelpers}

class ClientIdServiceSpec extends ItSpec {

  private val systemUnderTest = app.injector.instanceOf[ClientIdService]

  "ClientIdService" - {

    "should return the correct client id for a given tax regime when language is english" in {
      systemUnderTest.determineClientId(TestJourneys.PfSa.journeyBeforeBeginWebPayment, Languages.English) shouldBe ClientIds.SAEE
      systemUnderTest.determineClientId(TestJourneys.BtaSa.journeyBeforeBeginWebPayment, Languages.English) shouldBe ClientIds.SAEE
      systemUnderTest.determineClientId(TestJourneys.PtaSa.journeyBeforeBeginWebPayment, Languages.English) shouldBe ClientIds.SAEE
      systemUnderTest.determineClientId(TestJourneys.ItSa.journeyBeforeBeginWebPayment, Languages.English) shouldBe ClientIds.SAEE
      systemUnderTest.determineClientId(TestJourneys.PfAlcoholDuty.journeyBeforeBeginWebPayment, Languages.English) shouldBe ClientIds.ETEE
      systemUnderTest.determineClientId(TestJourneys.AlcoholDuty.journeyBeforeBeginWebPayment, Languages.English) shouldBe ClientIds.ETEE
    }

    "should return the correct client id for a given tax regime when language is welsh" in {
      systemUnderTest.determineClientId(TestJourneys.PfSa.journeyBeforeBeginWebPayment, Languages.Welsh) shouldBe ClientIds.SAEC
      systemUnderTest.determineClientId(TestJourneys.BtaSa.journeyBeforeBeginWebPayment, Languages.Welsh) shouldBe ClientIds.SAEC
      systemUnderTest.determineClientId(TestJourneys.PtaSa.journeyBeforeBeginWebPayment, Languages.Welsh) shouldBe ClientIds.SAEC
      systemUnderTest.determineClientId(TestJourneys.ItSa.journeyBeforeBeginWebPayment, Languages.Welsh) shouldBe ClientIds.SAEC
      systemUnderTest.determineClientId(TestJourneys.PfAlcoholDuty.journeyBeforeBeginWebPayment, Languages.Welsh) shouldBe ClientIds.ETEC
      systemUnderTest.determineClientId(TestJourneys.AlcoholDuty.journeyBeforeBeginWebPayment, Languages.Welsh) shouldBe ClientIds.ETEC
    }

  }

  "sanity check for implemented origins" in {
    TestHelpers.implementedOrigins.size shouldBe 6 withClue "** This dummy test is here to remind you to update the tests above. Bump up the expected number when an origin is added to implemented origins **"
  }

}
