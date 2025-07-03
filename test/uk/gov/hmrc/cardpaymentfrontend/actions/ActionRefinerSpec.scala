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

package uk.gov.hmrc.cardpaymentfrontend.actions

import org.scalatest.Assertion
import payapi.cardpaymentjourney.model.journey.{Journey, JourneySpecificData}
import payapi.corcommon.model.PaymentStatuses
import play.api.mvc.{ActionRefiner, AnyContent, Result, Results}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.cardpaymentfrontend.config.ErrorHandler
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec
import uk.gov.hmrc.cardpaymentfrontend.testsupport.TestOps.FakeRequestOps
import uk.gov.hmrc.cardpaymentfrontend.testsupport.stubs.PayApiStub
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.TestJourneys

import scala.concurrent.Future

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

  "paymentStatusActionRefiners" - {

    val systemUnderTest: PaymentStatusActionRefiners = app.injector.instanceOf[PaymentStatusActionRefiners]
    val errorHandler: ErrorHandler = app.injector.instanceOf[ErrorHandler]

      def fakeRequest(journey: Journey[JourneySpecificData]): JourneyRequest[AnyContent] = new JourneyRequest(journey, FakeRequest().withSessionId())
      def technicalDifficultiesPage(journey: Journey[JourneySpecificData]): HtmlFormat.Appendable = errorHandler.technicalDifficulties()(fakeRequest(journey))
      def test(
          journey:        Journey[JourneySpecificData],
          refiner:        ActionRefiner[JourneyRequest, JourneyRequest],
          expectedResult: Result
      ): Assertion = {
        val request = fakeRequest(journey)
        val resultFromRefine = refiner.invokeBlock(request, { _: JourneyRequest[_] =>
          Future.successful(Results.Ok("test ok"))
        })
        resultFromRefine.futureValue shouldBe expectedResult
      }

    "paymentStatusActionRefiner should" - {

      "return result when journey is in sent state" in {
        test(TestJourneys.PfSa.journeyAfterBeginWebPayment, systemUnderTest.paymentStatusActionRefiner, Results.Ok("test ok"))
      }

      "return redirect to /payment-complete when journey is in completed state" in {
        test(TestJourneys.PfSa.journeyAfterSucceedDebitWebPayment, systemUnderTest.paymentStatusActionRefiner, Results.Redirect("/pay-by-card/payment-complete"))
      }
      "return redirect to /payment-cancelled when journey is in cancelled state" in {
        test(TestJourneys.PfSa.journeyAfterCancelWebPayment, systemUnderTest.paymentStatusActionRefiner, Results.Redirect("/pay-by-card/payment-cancelled"))
      }
      "return redirect to /payment-failed when journey is in failed state" in {
        test(TestJourneys.PfSa.journeyAfterFailWebPayment, systemUnderTest.paymentStatusActionRefiner, Results.Redirect("/pay-by-card/payment-failed"))
      }

      "error/bad request when journey state is Created" in {
        val journey = TestJourneys.PfSa.journeyBeforeBeginWebPayment.copy(status = PaymentStatuses.Created)
        test(journey, systemUnderTest.paymentStatusActionRefiner, Results.BadRequest(technicalDifficultiesPage(journey)))
      }
      "error/bad request when journey state is Validated" in {
        val journey = TestJourneys.PfSa.journeyBeforeBeginWebPayment.copy(status = PaymentStatuses.Validated)
        test(journey, systemUnderTest.paymentStatusActionRefiner, Results.BadRequest(technicalDifficultiesPage(journey)))
      }
      "error/bad request when journey state is SoftDecline" in {
        val journey = TestJourneys.PfSa.journeyBeforeBeginWebPayment.copy(status = PaymentStatuses.SoftDecline)
        test(journey, systemUnderTest.paymentStatusActionRefiner, Results.BadRequest(technicalDifficultiesPage(journey)))
      }
    }

    "iframePageActionRefiner should" - {

      "return result when journey is in created state" in {
        test(TestJourneys.PfSa.journeyBeforeBeginWebPayment, systemUnderTest.iframePageActionRefiner, Results.Ok("test ok"))
      }

      "return result when journey is in sent state" in {
        test(TestJourneys.PfSa.journeyAfterBeginWebPayment, systemUnderTest.iframePageActionRefiner, Results.Ok("test ok"))
      }

      "return redirect to /payment-complete when journey is in completed state" in {
        test(TestJourneys.PfSa.journeyAfterSucceedDebitWebPayment, systemUnderTest.iframePageActionRefiner, Results.Redirect("/pay-by-card/payment-complete"))
      }

      "return redirect to /payment-cancelled when journey is in cancelled state" in {
        test(TestJourneys.PfSa.journeyAfterCancelWebPayment, systemUnderTest.iframePageActionRefiner, Results.Redirect("/pay-by-card/payment-cancelled"))
      }

      "return redirect to /payment-failed when journey is in failed state" in {
        test(TestJourneys.PfSa.journeyAfterFailWebPayment, systemUnderTest.iframePageActionRefiner, Results.Redirect("/pay-by-card/payment-failed"))
      }

      "error/bad request when journey state is Validated" in {
        val journey = TestJourneys.PfSa.journeyBeforeBeginWebPayment.copy(status = PaymentStatuses.Validated)
        test(journey, systemUnderTest.iframePageActionRefiner, Results.BadRequest(technicalDifficultiesPage(journey)))
      }
      "error/bad request when journey state is SoftDecline" in {
        val journey = TestJourneys.PfSa.journeyBeforeBeginWebPayment.copy(status = PaymentStatuses.SoftDecline)
        test(journey, systemUnderTest.iframePageActionRefiner, Results.BadRequest(technicalDifficultiesPage(journey)))
      }
    }

  }

}
