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

import payapi.corcommon.model.JourneyId
import uk.gov.hmrc.cardpaymentfrontend.models.payapirequest.{BeginWebPaymentRequest, FailWebPaymentRequest, SucceedWebPaymentRequest}
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

    "findJourneyByJourneyId" - {
        def headerCarrierForTest(maybeSessionId: Option[SessionId]) = HeaderCarrier(sessionId = maybeSessionId)

      val testJourney = TestJourneys.PfSa.journeyBeforeBeginWebPayment

      "return None when no journey can be found for given journey id in pay-api" in {
        PayApiStub.stubForFindByJourneyId404(testJourney._id)
        implicit val hc: HeaderCarrier = headerCarrierForTest(Some(SessionId("no-journey-exists-for-this-session-id")))
        systemUnderTest.findJourneyByJourneyId(testJourney._id).futureValue shouldBe None
      }

      "return Some Journey when there is one found for a journey id in pay-api" in {
        PayApiStub.stubForFindByJourneyId2xx(testJourney._id)(testJourney)
        implicit val hc: HeaderCarrier = headerCarrierForTest(Some(uk.gov.hmrc.http.SessionId("some-valid-session-id")))
        systemUnderTest.findJourneyByJourneyId(testJourney._id).futureValue shouldBe Some(testJourney)
      }

      "propagate a 5xx error when pay-api returns a 5xx" in {
        PayApiStub.stubForFindByJourneyId5xx(testJourney._id)
        implicit val hc: HeaderCarrier = headerCarrierForTest(Some(uk.gov.hmrc.http.SessionId("some-valid-session-id")))
        val error: Exception = intercept[Exception](systemUnderTest.findJourneyByJourneyId(testJourney._id).futureValue)
        error.getCause.getMessage should include(s"GET of 'http://localhost:${wireMockPort.toString}/pay-api/journey/${testJourney._id.value}' returned 503.")
      }
    }

    "JourneyUpdates" - {

      implicit val headerCarrierForTest: HeaderCarrier = HeaderCarrier(sessionId = Some(uk.gov.hmrc.http.SessionId("some-valid-session-id")))

      "updateBeginWebPayment" - {
        "should send a BeginWebPaymentRequest to pay-api" in {
          val testJourney = TestJourneys.PfSa.journeyBeforeBeginWebPayment
          PayApiStub.stubForUpdateBeginWebPayment2xx(testJourney._id)
          systemUnderTest.JourneyUpdates.updateBeginWebPayment(testJourney._id.value, BeginWebPaymentRequest("sometransactionref", "someiframeurl")).futureValue
          PayApiStub.verifyUpdateBeginWebPayment(1, testJourney._id)
        }
      }
      "updateSucceedWebPayment" - {
        "should send a SucceedWebPaymentRequest to pay-api" in {
          val testJourney = TestJourneys.PfSa.journeyAfterBeginWebPayment
          PayApiStub.stubForUpdateSucceedWebPayment2xx(testJourney._id)
          systemUnderTest.JourneyUpdates.updateSucceedWebPayment(testJourney._id.value, SucceedWebPaymentRequest("debit", Some(123), FrozenTime.localDateTime)).futureValue
          PayApiStub.verifyUpdateSucceedWebPayment(1, testJourney._id, FrozenTime.localDateTime)
        }
      }
      "updateCancelWebPayment" - {
        "should call pay-api to set payment status to Cancelled" in {
          val testJourney = TestJourneys.PfSa.journeyAfterBeginWebPayment
          PayApiStub.stubForUpdateCancelWebPayment2xx(testJourney._id)
          systemUnderTest.JourneyUpdates.updateCancelWebPayment(testJourney._id.value).futureValue
          PayApiStub.verifyUpdateCancelWebPayment(1, testJourney._id)
        }
      }
      "updateFailWebPayment" - {
        "should send a FailWebPaymentRequest to pay-api" in {
          val testJourney = TestJourneys.PfSa.journeyAfterBeginWebPayment
          PayApiStub.stubForUpdateFailWebPayment2xx(testJourney._id)
          systemUnderTest.JourneyUpdates.updateFailWebPayment(testJourney._id.value, FailWebPaymentRequest(FrozenTime.localDateTime, "debit")).futureValue
          PayApiStub.verifyUpdateFailWebPayment(1, testJourney._id, FrozenTime.localDateTime)
        }
      }
    }

    "restartJourneyAsNew" - {

      val testJourney = TestJourneys.PfSa.journeyAfterFailWebPayment

      "should return a journeyId when call to pay-api succeeds" in {
        PayApiStub.stubForCloneJourney2xx(testJourney._id)
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("some-valid-session-id")))
        systemUnderTest.restartJourneyAsNew(testJourney._id).futureValue shouldBe JourneyId("TestJourneyId-44f9-ad7f-01e1d3d8f151")
      }

      "propagate a 5xx error when pay-api returns a 5xx" in {
        PayApiStub.stubForCloneJourney5xx(testJourney._id)
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("some-valid-session-id")))
        val error: Exception = intercept[Exception](systemUnderTest.restartJourneyAsNew(testJourney._id).futureValue)
        error.getCause.getMessage should include(s"POST of 'http://localhost:6001/pay-api/journey/TestJourneyId-44f9-ad7f-01e1d3d8f151/restart-journey-as-new' returned 500.")
      }
    }
  }
}
