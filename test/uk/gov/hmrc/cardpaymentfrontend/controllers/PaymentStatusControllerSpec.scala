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
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.mvc.Http.Status
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec
import uk.gov.hmrc.cardpaymentfrontend.testsupport.TestOps.FakeRequestOps
import uk.gov.hmrc.cardpaymentfrontend.testsupport.stubs.PayApiStub
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.TestJourneys
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl

class PaymentStatusControllerSpec extends ItSpec {

  val systemUnderTest: PaymentStatusController = app.injector.instanceOf[PaymentStatusController]

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
        val result = systemUnderTest.showIframe(RedirectUrl("http://localhost:8080"))(fakeRequest)
        val document = Jsoup.parse(contentAsString(result))
        document.select(".hmrc-language-select__list-item").isEmpty shouldBe true
      }
    }

    "returnToHmrc" - {

      "should render the redirectToParent page which links to the /payment-status endpoint" in {
        val result = systemUnderTest.returnToHmrc()(fakeRequest)
        status(result) shouldBe Status.OK
        val document = Jsoup.parse(contentAsString(result))
        val anchorElement = document.select("#returnControlLink")
        anchorElement.attr("href") shouldBe "/pay-by-card/payment-status"
        anchorElement.attr("target") shouldBe "_parent"
      }

    }
  }

}
