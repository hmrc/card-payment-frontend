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

package uk.gov.hmrc.cardpaymentfrontend.controllers

import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, redirectLocation}
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking.{CreateSessionDataResponse, SessionDataId}
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec
import uk.gov.hmrc.cardpaymentfrontend.testsupport.TestOps.FakeRequestOps
import uk.gov.hmrc.cardpaymentfrontend.testsupport.stubs.{OpenBankingStub, PayApiStub}
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.TestJourneys

class OpenBankingControllerSpec extends ItSpec {

  val systemUnderTest: OpenBankingController = app.injector.instanceOf[OpenBankingController]

  "OpenBankingController" - {
    "startOpenBankingJourney" - {
      "should throw an error when a createSessionDataRequest cannot be created" in {
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.testPfSaJourneyCreated)
        val fakeRequest = FakeRequest().withSessionId()
        val exception = intercept[RuntimeException] {
          systemUnderTest.startOpenBankingJourney(fakeRequest).futureValue
        }
        exception.getMessage shouldBe "The future returned an exception of type: java.lang.RuntimeException, with message: Unable to build createSessionDataRequest, so cannot start an OB journey."
      }

      "redirect when call to open banking to start a journey succeeds" in {
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.testPfSaJourneyUpdatedWithRefAndAmount)
        OpenBankingStub.stubForStartJourney2xx(CreateSessionDataResponse(SessionDataId("some-session-data-id"), "http://www.some-redirect-url-for-ob.co.uk"))
        val fakeRequest = FakeRequest().withSessionId()
        val result = systemUnderTest.startOpenBankingJourney(fakeRequest)
        redirectLocation(result) shouldBe Some("http://www.some-redirect-url-for-ob.co.uk")
      }
    }
  }

}
