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

package uk.gov.hmrc.cardpaymentfrontend.connectors

import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec
import uk.gov.hmrc.cardpaymentfrontend.testsupport.stubs.PayApiStub
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.TestJourneys
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}

class PayApiConnectorSpec extends ItSpec {

  val systemUnderTest: PayApiConnector = app.injector.instanceOf[PayApiConnector]

  "PayApiConnector" - {

    "findLatestJourneyBySessionId" - {

        def headerCarrierForTest(maybeSessionId: Option[SessionId]) = HeaderCarrier(sessionId = maybeSessionId)

      "should fail with error when no session id provided through header carrier" in {
        implicit val hc: HeaderCarrier = headerCarrierForTest(None)
        val thrown = systemUnderTest.findLatestJourneyBySessionId().failed.futureValue
        thrown.getMessage should include("Missing required 'SessionId'")
      }

      "return None when no journey can be found for given session id in pay-api" in {
        PayApiStub.stubForFindBySessionId404
        implicit val hc: HeaderCarrier = headerCarrierForTest(Some(SessionId("no-journey-exists-for-this-session-id")))
        systemUnderTest.findLatestJourneyBySessionId().futureValue shouldBe None
      }

      "return Some Journey when there is one found for a session id in pay-api" in {
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
        implicit val hc: HeaderCarrier = headerCarrierForTest(Some(uk.gov.hmrc.http.SessionId("some-valid-session-id")))
        systemUnderTest.findLatestJourneyBySessionId().futureValue shouldBe Some(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
      }

      "propagate a 5xx error when pay-api returns a 5xx" in {
        PayApiStub.stubForFindBySessionId5xx
        implicit val hc: HeaderCarrier = headerCarrierForTest(Some(uk.gov.hmrc.http.SessionId("some-valid-session-id")))
        val error: Exception = intercept[Exception](systemUnderTest.findLatestJourneyBySessionId().futureValue)
        error.getCause.getMessage should include(s"GET of 'http://localhost:${wireMockPort.toString}/pay-api/journey/find-latest-by-session-id' returned 503.")
      }
    }

    "JourneyUpdates" - {
      "updateBeginWebPayment" is pending
      "updateSucceedWebPayment" is pending
      "updateCancelWebPayment" is pending
      "updateFailWebPayment" is pending
    }
  }
}
