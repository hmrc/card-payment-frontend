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

import org.jsoup.Jsoup
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import sttp.model.HeaderNames.Cookie
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec

class PaymentFailedControllerSpec extends ItSpec {

  private val systemUnderTest: PaymentFailedController = app.injector.instanceOf[PaymentFailedController]

  "PaymentFailedController" - {

    "GET /payment-failed" - {

      val fakeGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/payment-failed")
      val fakeGetRequestInWelsh: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/email-address").withCookies(Cookie("PLAY_LANG", "cy"))

      "should return 200 OK" in {
        val result = systemUnderTest.renderPage(fakeGetRequest)
        status(result) shouldBe Status.OK
      }

      "render the page with the correct sub heading in English" in {
        val result = systemUnderTest.renderPage(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        document.selectXpath("//*[@id=\"main-content\"]/div/div/p[1]").text() shouldBe "No payment has been taken from your card."
      }

      "render the page with the correct line 1 in English" in {
        val result = systemUnderTest.renderPage(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        document.selectXpath("//*[@id=\"main-content\"]/div/div/p[2]").text() shouldBe "The payment may have failed if:"
      }

      "render the page with the correct line 2 in English" in {
        val result = systemUnderTest.renderPage(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        document.selectXpath("//*[@id=\"main-content\"]/div/div/ul/li[1]").text() shouldBe "there are not enough funds in your account"
      }

      "render the page with the correct line 3 in English" in {
        val result = systemUnderTest.renderPage(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        document.selectXpath("//*[@id=\"main-content\"]/div/div/ul/li[2]").text() shouldBe "you entered invalid or expired card details"
      }

      "render the page with the correct line 4 in English" in {
        val result = systemUnderTest.renderPage(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        document.selectXpath("//*[@id=\"main-content\"]/div/div/ul/li[3]").text() shouldBe "the address you gave does not match the one your card issuer has"
      }

      "render the page with the correct check again content in English" in {
        val result = systemUnderTest.renderPage(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        document.selectXpath("//*[@id=\"main-content\"]/div/div/ul/li[3]").text() shouldBe "the address you gave does not match the one your card issuer has"
      }

      "render the page with the correct sub heading in Welsh" in {
        val result = systemUnderTest.renderPage(fakeGetRequestInWelsh)
        val document = Jsoup.parse(contentAsString(result))
        document.selectXpath("//*[@id=\"main-content\"]/div/div/p[1]").text() shouldBe "No payment has been taken from your card."
      }

      "render the page with the correct line 1 in Welsh" in {
        val result = systemUnderTest.renderPage(fakeGetRequestInWelsh)
        val document = Jsoup.parse(contentAsString(result))
        document.selectXpath("//*[@id=\"main-content\"]/div/div/p[2]").text() shouldBe "The payment may have failed if:"
      }

      "render the page with the correct line 2 in Welsh" in {
        val result = systemUnderTest.renderPage(fakeGetRequestInWelsh)
        val document = Jsoup.parse(contentAsString(result))
        document.selectXpath("//*[@id=\"main-content\"]/div/div/ul/li[1]").text() shouldBe "there are not enough funds in your account"
      }

      "render the page with the correct line 3 in Welsh" in {
        val result = systemUnderTest.renderPage(fakeGetRequestInWelsh)
        val document = Jsoup.parse(contentAsString(result))
        document.selectXpath("//*[@id=\"main-content\"]/div/div/ul/li[2]").text() shouldBe "you entered invalid or expired card details"
      }

      "render the page with the correct line 4 in Welsh" in {
        val result = systemUnderTest.renderPage(fakeGetRequestInWelsh)
        val document = Jsoup.parse(contentAsString(result))
        document.selectXpath("//*[@id=\"main-content\"]/div/div/ul/li[3]").text() shouldBe "the address you gave does not match the one your card issuer has"
      }

    }

  }

}
