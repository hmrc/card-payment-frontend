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

package uk.gov.hmrc.cardpaymentfrontend.connectors

import payapi.corcommon.model.AmountInPence
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking.{CreateSessionDataRequest, CreateSessionDataResponse, SessionDataId}
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec
import uk.gov.hmrc.cardpaymentfrontend.testsupport.stubs.OpenBankingStub
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.TestOpenBankingData.TestOriginSpecificSessionData.pfSaOriginSpecificData
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}

class OpenBankingConnectorSpec extends ItSpec {

  val systemUnderTest: OpenBankingConnector = app.injector.instanceOf[OpenBankingConnector]

  "OpenBankingConnector" - {
    "startOpenBankingJourney" - {
      def headerCarrierForTest(maybeSessionId: Option[SessionId]) = HeaderCarrier(sessionId = maybeSessionId)

      val createSessionDataRequest = CreateSessionDataRequest(AmountInPence(123), pfSaOriginSpecificData, None)

      "should fail with error when no session id provided through header carrier" in {
        implicit val hc: HeaderCarrier = headerCarrierForTest(None)
        val thrown                     = systemUnderTest.startOpenBankingJourney(createSessionDataRequest).failed.futureValue
        thrown.getMessage should include("Missing required 'SessionId'")
      }

      "propagate a 5xx error when pay-api returns a 5xx" in {
        OpenBankingStub.stubForStartJourney5xx()
        implicit val hc: HeaderCarrier = headerCarrierForTest(Some(uk.gov.hmrc.http.SessionId("some-valid-session-id")))
        val error: Exception           = intercept[Exception](systemUnderTest.startOpenBankingJourney(createSessionDataRequest).futureValue)
        error.getCause.getMessage should include(s"POST of 'http://localhost:${wireMockPort.toString}/open-banking/session' returned 503.")
      }

      "return a CreateSessionDataResponse if session id provided in the header carrier" in {
        val expectedCreateSessionDataResponse = CreateSessionDataResponse(SessionDataId("test-session-data-id"), "https://www.some-next-url.com")
        OpenBankingStub.stubForStartJourney2xx(expectedCreateSessionDataResponse)
        implicit val hc: HeaderCarrier        = headerCarrierForTest(Some(uk.gov.hmrc.http.SessionId("some-valid-session-id")))
        systemUnderTest.startOpenBankingJourney(createSessionDataRequest).futureValue shouldBe expectedCreateSessionDataResponse
      }
    }
  }

}
