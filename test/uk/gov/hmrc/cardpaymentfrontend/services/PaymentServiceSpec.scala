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

import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.time.{Milliseconds, Span}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec
import uk.gov.hmrc.cardpaymentfrontend.testsupport.stubs.PayApiStub
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.TestJourneys

class PaymentServiceSpec extends ItSpec {

  val systemUnderTest: PaymentService = app.injector.instanceOf[PaymentService]

  "PaymentService" - {
    "resetSentJourney" - {
      "should trigger a call to pay-api to reset the order when order is present in journey" in {
        val testJourney = TestJourneys.PfSa.journeyAfterBeginWebPayment
        val fakeRequest = FakeRequest("GET", "/blah")
        implicit val journeyRequest: JourneyRequest[AnyContentAsEmpty.type] = new JourneyRequest(testJourney, fakeRequest)
        val _ = systemUnderTest.resetSentJourney().futureValue
        eventually(Timeout(Span(500, Milliseconds))) { PayApiStub.verifyResetWebPayment(1, testJourney._id) }
      }
      "should not trigger a call to pay-api to reset the order when order is None in journey" in {
        val testJourney = TestJourneys.PfSa.journeyBeforeBeginWebPayment
        val fakeRequest = FakeRequest("GET", "/blah")
        implicit val journeyRequest: JourneyRequest[AnyContentAsEmpty.type] = new JourneyRequest(testJourney, fakeRequest)
        val _ = systemUnderTest.resetSentJourney().futureValue
        eventually(Timeout(Span(500, Milliseconds))) { PayApiStub.verifyResetWebPayment(0, testJourney._id) }
      }
    }
  }

}
