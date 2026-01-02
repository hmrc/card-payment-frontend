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
import payapi.cardpaymentjourney.model.journey.{Journey, JourneySpecificData, Url}
import payapi.corcommon.model.{Origin, Origins, PaymentStatuses}
import play.api.i18n.MessagesApi
import play.api.mvc.{ActionRefiner, AnyContent, Result, Results}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.cardpaymentfrontend.config.ErrorHandler
import uk.gov.hmrc.cardpaymentfrontend.testsupport.TestOps.FakeRequestOps
import uk.gov.hmrc.cardpaymentfrontend.testsupport.stubs.PayApiStub
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.{TestJourneys, TestPayApiData}
import uk.gov.hmrc.cardpaymentfrontend.testsupport.{ItSpec, TestHelpers}
import uk.gov.hmrc.cardpaymentfrontend.views.html.ForceDeleteAnswersPage

import scala.concurrent.Future

class ActionRefinerSpec extends ItSpec {

  "getJourneyActionRefiner" - {

    val systemUnderTest: GetJourneyActionRefiner = app.injector.instanceOf[GetJourneyActionRefiner]

    val fakeRequest: FakeRequest[AnyContent] = FakeRequest("GET", "/who-cares").withSessionId()

    val forceDeleteAnswersPage = app.injector.instanceOf[ForceDeleteAnswersPage]
    val messagesApi            = app.injector.instanceOf[MessagesApi]

    "should return a left with unauthorised Result when pay-api returns no journey" in {
      systemUnderTest.refine(fakeRequest).futureValue shouldBe Left(
        Results.Unauthorized(forceDeleteAnswersPage(false, Some(Url("http://localhost:9056/pay")))(fakeRequest, messagesApi.preferred(fakeRequest)))
      )
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

    val errorHandler = app.injector.instanceOf[ErrorHandler]

    "should return a left when journey in JourneyRequest is not in a 'terminal state' (i.e. finished)" in {
      val request = fakeRequest(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
      systemUnderTest.refine(request).futureValue shouldBe Left(Results.Gone(errorHandler.notFoundTemplate(request).futureValue))
    }

    "should return a right when journey in JourneyRequest is in a 'terminal state' (i.e. finished)" in {
      val request                                            = fakeRequest(TestJourneys.PfSa.journeyAfterSucceedDebitWebPayment)
      val result: Either[Result, JourneyRequest[AnyContent]] = systemUnderTest.refine(request).futureValue
      result.isRight shouldBe true
      result.map(_.journey) shouldBe Right(TestJourneys.PfSa.journeyAfterSucceedDebitWebPayment)
    }
  }

  "journeyRoutingActionRefiner" - {
    val systemUnderTest: JourneyRoutingActionRefiner = app.injector.instanceOf[JourneyRoutingActionRefiner]

    def fakeRequest(journey: Journey[JourneySpecificData]): JourneyRequest[AnyContent] = new JourneyRequest(journey, FakeRequest())

    "should return a Left with redirect to address page" - {
      "for Mib origin" in {
        val request = fakeRequest(TestJourneys.Mib.journeyBeforeBeginWebPayment)
        systemUnderTest.refine(request).futureValue shouldBe Left(Results.Redirect("/pay-by-card/address"))
      }
      "for BcPngr origin" in {
        val request = fakeRequest(TestJourneys.BcPngr.journeyBeforeBeginWebPayment)
        systemUnderTest.refine(request).futureValue shouldBe Left(Results.Redirect("/pay-by-card/address"))
      }
    }

    "should return a Right when origin is not Mib or BcPngr" in {
      TestHelpers.implementedOrigins
        .diff[Origin](Seq[Origin](Origins.Mib, Origins.BcPngr))
        .foreach { origin =>
          val request                                            = fakeRequest(TestHelpers.deriveTestDataFromOrigin(origin).journeyBeforeBeginWebPayment)
          val result: Either[Result, JourneyRequest[AnyContent]] = systemUnderTest.refine(request).futureValue
          result.isRight shouldBe true withClue s"expected a right for origin: ${origin.entryName}"
        }
    }
  }

  "paymentStatusActionRefiners" - {

    val systemUnderTest: PaymentStatusActionRefiners = app.injector.instanceOf[PaymentStatusActionRefiners]
    val errorHandler: ErrorHandler                   = app.injector.instanceOf[ErrorHandler]
    val forceDeleteAnswersPage                       = app.injector.instanceOf[ForceDeleteAnswersPage]
    val messagesApi                                  = app.injector.instanceOf[MessagesApi]

    def fakeRequest(journey:               Journey[JourneySpecificData]): JourneyRequest[AnyContent] = new JourneyRequest(journey, FakeRequest().withSessionId())
    def technicalDifficultiesPage(journey: Journey[JourneySpecificData]): HtmlFormat.Appendable      = errorHandler.technicalDifficulties()(fakeRequest(journey))
    def test(
      journey:        Journey[JourneySpecificData],
      refiner:        ActionRefiner[JourneyRequest, JourneyRequest],
      expectedResult: Result
    ): Assertion = {
      val request          = fakeRequest(journey)
      val resultFromRefine = refiner.invokeBlock(
        request,
        { _: JourneyRequest[_] =>
          Future.successful(Results.Ok("test ok"))
        }
      )
      resultFromRefine.futureValue shouldBe expectedResult
    }

    "findJourneyBySessionIdFallBackToJourneyIdRefiner should" - {

      "return result when journey is found by session id, not needing to find by journey id" in {
        val testJourney = TestJourneys.PfSa.journeyAfterBeginWebPayment
        PayApiStub.stubForFindBySessionId2xx(testJourney)
        test(
          journey = testJourney,
          refiner = systemUnderTest.findJourneyBySessionIdFallBackToJourneyIdRefiner(TestPayApiData.base64EncryptedJourneyId),
          expectedResult = Results.Ok("test ok")
        )
        PayApiStub.verifyFindByJourneyId(0, testJourney._id)
      }

      "return result when journey is not found by session id but then found by journey id" in {
        val testJourney = TestJourneys.PfSa.journeyAfterBeginWebPayment
        PayApiStub.stubForFindBySessionId404
        PayApiStub.stubForFindByJourneyId2xx(testJourney._id)(testJourney)
        test(
          journey = testJourney,
          refiner = systemUnderTest.findJourneyBySessionIdFallBackToJourneyIdRefiner(TestPayApiData.base64EncryptedJourneyId),
          expectedResult = Results.Ok("test ok")
        )
      }

      "return Unauthorized with force delete answers page when no journey can be found by payment session id or journey id" in {
        val testJourney = TestJourneys.PfSa.journeyAfterBeginWebPayment
        PayApiStub.stubForFindBySessionId404
        PayApiStub.stubForFindByJourneyId404(testJourney._id)
        test(
          journey = testJourney,
          refiner = systemUnderTest.findJourneyBySessionIdFallBackToJourneyIdRefiner(TestPayApiData.base64EncryptedJourneyId),
          expectedResult = Results.Unauthorized(
            forceDeleteAnswersPage(false, Some(Url("http://localhost:9056/pay")))(fakeRequest(testJourney), messagesApi.preferred(fakeRequest(testJourney)))
          )
        )
      }
    }

    "paymentStatusActionRefiner should" - {

      "return result when journey is in sent state" in {
        test(TestJourneys.PfSa.journeyAfterBeginWebPayment, systemUnderTest.paymentStatusActionRefiner, Results.Ok("test ok"))
      }

      "return redirect to /payment-complete when journey is in completed state" in {
        test(
          TestJourneys.PfSa.journeyAfterSucceedDebitWebPayment,
          systemUnderTest.paymentStatusActionRefiner,
          Results.Redirect("/pay-by-card/payment-complete")
        )
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
