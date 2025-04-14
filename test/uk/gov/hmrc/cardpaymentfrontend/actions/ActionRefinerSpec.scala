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

package uk.gov.hmrc.cardpaymentfrontend.actions

import payapi.cardpaymentjourney.model.journey.{Journey, JourneySpecificData}
import play.api.mvc.{AnyContent, Result, Results}
import play.api.test.FakeRequest
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec
import uk.gov.hmrc.cardpaymentfrontend.testsupport.TestOps.FakeRequestOps
import uk.gov.hmrc.cardpaymentfrontend.testsupport.stubs.PayApiStub
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.TestJourneys

class ActionRefinerSpec extends ItSpec {

  "getJourneyActionRefiner" - {

    val systemUnderTest: GetJourneyActionRefiner = app.injector.instanceOf[GetJourneyActionRefiner]

    val fakeRequest: FakeRequest[AnyContent] = FakeRequest("GET", "/who-cares").withSessionId()

    "should return a left with unauthorised Result when pay-api returns no journey" in {
      systemUnderTest.refine(fakeRequest).futureValue shouldBe Left(Results.Unauthorized("need a session id"))
    }

    "should return a right with JourneyRequest when pay-api returns a journey" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
      val result: Either[Result, JourneyRequest[AnyContent]] = systemUnderTest.refine(fakeRequest).futureValue
      result.isRight shouldBe true
      result.map(_.journey) shouldBe Right(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
    }
  }

  "journeyFinishedActionRefiner" - {

    val systemUnderTest: JourneyFinishedActionRefiner = app.injector.instanceOf[JourneyFinishedActionRefiner]

      def fakeRequest(journey: Journey[JourneySpecificData]): JourneyRequest[AnyContent] = new JourneyRequest(journey, FakeRequest())

    "should return a left when journey in JourneyRequest is not in a 'terminal state' (i.e. finished)" in {
      val request = fakeRequest(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
      systemUnderTest.refine(request).futureValue shouldBe Left(Results.NotFound("Journey not in valid state"))
    }

    "should return a right when journey in JourneyRequest is in a 'terminal state' (i.e. finished)" in {
      val request = fakeRequest(TestJourneys.PfSa.journeyAfterSucceedDebitWebPayment)
      val result: Either[Result, JourneyRequest[AnyContent]] = systemUnderTest.refine(request).futureValue
      result.isRight shouldBe true
      result.map(_.journey) shouldBe Right(TestJourneys.PfSa.journeyAfterSucceedDebitWebPayment)
    }
  }

}
