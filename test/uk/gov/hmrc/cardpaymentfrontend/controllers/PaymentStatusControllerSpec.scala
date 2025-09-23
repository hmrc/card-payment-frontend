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

package uk.gov.hmrc.cardpaymentfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.mvc.Http.Status
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.cardpayment.{AdditionalPaymentInfo, CardPaymentFinishPaymentResponses, CardPaymentResult}
import uk.gov.hmrc.cardpaymentfrontend.services.CryptoService
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec
import uk.gov.hmrc.cardpaymentfrontend.testsupport.TestOps.FakeRequestOps
import uk.gov.hmrc.cardpaymentfrontend.testsupport.stubs.{CardPaymentStub, PayApiStub}
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.{TestJourneys, TestPayApiData}
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl

import scala.jdk.CollectionConverters.CollectionHasAsScala

class PaymentStatusControllerSpec extends ItSpec {

  val systemUnderTest: PaymentStatusController = app.injector.instanceOf[PaymentStatusController]

  val cryptoService: CryptoService = app.injector.instanceOf[CryptoService]

  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSessionId()

  "PaymentStatusController" - {

    "showIframe" - {

      "should return 200 OK and render a hmrc looking page with the provided url in an iframe" in {
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyAfterBeginWebPayment)
        val validIframeUrl = "http://localhost:8080"
        val result = systemUnderTest.showIframe(RedirectUrl(validIframeUrl))(fakeRequest)

        status(result) shouldBe Status.OK
        val document = Jsoup.parse(contentAsString(result))
        document.title shouldBe "Make your payment - Pay your Self Assessment - GOV.UK"
        document.select("h1").text() shouldBe "Make your payment"
        document.select(".govuk-header__service-name").text() shouldBe "Pay your Self Assessment"

        val iframe = document.select("iframe")
        iframe.attr("title") shouldBe "Make your payment"
        iframe.attr("src") shouldBe validIframeUrl
      }

      "should throw an IllegalArgumentException when the iframe url provided is not a valid url" in {
        val exception = intercept[IllegalArgumentException](systemUnderTest.showIframe(RedirectUrl("invalid"))(fakeRequest).futureValue)
        exception.getMessage shouldBe "requirement failed: 'invalid' is not a valid continue URL"
      }

      "should return BAD_REQUEST when iframe url does not comply with redirect policy (i.e. host not on allow list in config)" in {
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyAfterBeginWebPayment)
        val result = systemUnderTest.showIframe(RedirectUrl("https://www.gov.uk"))(fakeRequest)
        status(result) shouldBe Status.BAD_REQUEST
        contentAsString(result) shouldBe "Bad url provided that doesn't match the redirect policy. Check allow list if this is not expected."
      }

      "should not render the iframe page with a language toggle" in {
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyAfterBeginWebPayment)
        val result = systemUnderTest.showIframe(RedirectUrl("http://localhost:8080"))(fakeRequest)
        val document = Jsoup.parse(contentAsString(result))
        document.select(".hmrc-language-select__list-item").isEmpty shouldBe true
      }

      "should render the iframe page with the iframe scroll javascript fix" in {
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyAfterBeginWebPayment)
        val result = systemUnderTest.showIframe(RedirectUrl("http://localhost:8080"))(fakeRequest)
        val document = Jsoup.parse(contentAsString(result))
        document.select("script").asScala.toList.map(_.toString) should contain("<script src=\"/pay-by-card/assets/javascripts/iframe-scroll.js\"></script>")
      }
    }

    "returnToHmrc" - {

      "should render the redirectToParent page which links to the /payment-status endpoint, while keeping the base 64 encoded encrypted journeyid in the path param" in {
        val base64EncodedEncryptedJourneyId: String = TestPayApiData.base64EncryptedJourneyId
        val result = systemUnderTest.returnToHmrc(base64EncodedEncryptedJourneyId)(fakeRequest)
        status(result) shouldBe Status.OK
        val document = Jsoup.parse(contentAsString(result))
        val anchorElement = document.select("#returnControlLink")
        anchorElement.attr("href") shouldBe s"/pay-by-card/payment-status/$base64EncodedEncryptedJourneyId"
        anchorElement.attr("target") shouldBe "_parent"
      }

    }

    "paymentStatus" - {

      "should return bad request due to action refiner when journey state is before Sent, i.e. Created" in {
        val journeyBeforeBeginWebPayment = TestJourneys.PfSa.journeyBeforeBeginWebPayment
        PayApiStub.stubForFindByJourneyId2xx(journeyBeforeBeginWebPayment._id)(journeyBeforeBeginWebPayment)
        val result = systemUnderTest.paymentStatus(TestPayApiData.base64EncryptedJourneyId)(fakeRequest)
        status(result) shouldBe Status.BAD_REQUEST
      }

      "should return a redirect to payment success page when Successful CardPaymentResult is returned from backend after AuthAndCapture" in {
        val journey = TestJourneys.PfSa.journeyAfterBeginWebPayment
        val fakeRequest = new JourneyRequest(journey, FakeRequest().withSessionId())
        PayApiStub.stubForFindByJourneyId2xx(journey._id)(journey)
        val testCardPaymentResult = CardPaymentResult(CardPaymentFinishPaymentResponses.Successful, AdditionalPaymentInfo(Some("debit"), Some(123), Some(FrozenTime.localDateTime)))
        CardPaymentStub.AuthAndCapture.stubForAuthAndCapture2xx("Some-transaction-ref", testCardPaymentResult)
        val result = systemUnderTest.paymentStatus(TestPayApiData.base64EncryptedJourneyId)(fakeRequest)
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/pay-by-card/payment-complete")
      }

      "should return a redirect to payment failed page when Failed CardPaymentResult is returned from backend after AuthAndCapture" in {
        val journey = TestJourneys.PfSa.journeyAfterBeginWebPayment
        val fakeRequest = new JourneyRequest(journey, FakeRequest().withSessionId())
        PayApiStub.stubForFindByJourneyId2xx(journey._id)(journey)
        val testCardPaymentResult = CardPaymentResult(CardPaymentFinishPaymentResponses.Failed, AdditionalPaymentInfo(Some("debit"), Some(123), Some(FrozenTime.localDateTime)))
        CardPaymentStub.AuthAndCapture.stubForAuthAndCapture2xx("Some-transaction-ref", testCardPaymentResult)
        val result = systemUnderTest.paymentStatus(TestPayApiData.base64EncryptedJourneyId)(fakeRequest)
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/pay-by-card/payment-failed")
      }

      "should return a redirect to payment cancelled page when Cancelled CardPaymentResult is returned from backend after AuthAndCapture" in {
        val journey = TestJourneys.PfSa.journeyAfterBeginWebPayment
        val fakeRequest = new JourneyRequest(journey, FakeRequest().withSessionId())
        PayApiStub.stubForFindByJourneyId2xx(journey._id)(journey)
        val testCardPaymentResult = CardPaymentResult(CardPaymentFinishPaymentResponses.Cancelled, AdditionalPaymentInfo(Some("debit"), Some(123), Some(FrozenTime.localDateTime)))
        CardPaymentStub.AuthAndCapture.stubForAuthAndCapture2xx("Some-transaction-ref", testCardPaymentResult)
        val result = systemUnderTest.paymentStatus(TestPayApiData.base64EncryptedJourneyId)(fakeRequest)
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/pay-by-card/payment-cancelled")
      }

      "should throw an exception/error when there's no transaction reference in the order, shouldn't be possible, i.e. order is None, even though we've initiated a payment, and not call backend to auth and settle" in {
        val journey = TestJourneys.PfSa.journeyAfterBeginWebPayment.copy(order = None)
        val fakeRequest = new JourneyRequest(journey, FakeRequest().withSessionId())
        PayApiStub.stubForFindByJourneyId2xx(journey._id)(journey)
        val error = intercept[RuntimeException] {
          systemUnderTest.paymentStatus(TestPayApiData.base64EncryptedJourneyId)(fakeRequest).futureValue
        }
        error.getMessage shouldBe "The future returned an exception of type: java.lang.RuntimeException, with message: Could not find transaction ref, therefore we can't auth and settle.."
        CardPaymentStub.AuthAndCapture.verifyNone("Some-transaction-ref")
      }

      "should send a cancel request if the call to auth and settle fails for whatever unexpected reason, but still propagate the error" in {
        val journey = TestJourneys.PfSa.journeyAfterBeginWebPayment
        val fakeRequest = new JourneyRequest(journey, FakeRequest().withSessionId())
        PayApiStub.stubForFindByJourneyId2xx(journey._id)(journey)
        CardPaymentStub.AuthAndCapture.stubForAuthAndCapture5xx("Some-transaction-ref")
        val result = systemUnderTest.paymentStatus(TestPayApiData.base64EncryptedJourneyId)(fakeRequest)
        status(result) shouldBe 500
        CardPaymentStub.CancelPayment.verifyOne("Some-transaction-ref", "SAEE")
      }

      "should throw an InternalServerError when the response from backend can't be deserialised as expected" in {
        val journey = TestJourneys.PfSa.journeyAfterBeginWebPayment
        val fakeRequest = new JourneyRequest(journey, FakeRequest().withSessionId())
        PayApiStub.stubForFindByJourneyId2xx(journey._id)(journey)
        CardPaymentStub.AuthAndCapture.stubForAuthAndCaptureCustomJson2xx("Some-transaction-ref", Json.parse("""{"some":"invalidjson"}"""))
        val result = systemUnderTest.paymentStatus(TestPayApiData.base64EncryptedJourneyId)(fakeRequest)
        status(result) shouldBe 500
        CardPaymentStub.CancelPayment.verifyOne("Some-transaction-ref", "SAEE")
      }

    }
  }

}
