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
import play.api.mvc.{AnyContentAsEmpty, Cookie}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.cardpaymentfrontend.forms.ChooseAPaymentMethodForm
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec

import scala.jdk.CollectionConverters.ListHasAsScala

class PaymentFailedControllerSpec extends ItSpec {

  private val systemUnderTest: PaymentFailedController = app.injector.instanceOf[PaymentFailedController]

  "PaymentFailedController" - {

    "GET /payment-failed" - {

      val fakeGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/payment-failed")
      val fakeGetRequestInWelsh: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/payment-failed").withCookies(Cookie("PLAY_LANG", "cy"))

      "should return 200 OK" in {
        val result = systemUnderTest.renderPage(fakeGetRequest)
        status(result) shouldBe Status.OK
      }

      "include the hmrc layout" in {
        val result = systemUnderTest.renderPage(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        document.select("html").hasClass("govuk-template") shouldBe true withClue "no govuk template"
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
        document.selectXpath("//*[@id=\"main-content\"]/div/div/p[1]").text() shouldBe "Nid oes taliad wedi’i dynnu o’ch cerdyn."
      }

      "render the page with the correct line 1 in Welsh" in {
        val result = systemUnderTest.renderPage(fakeGetRequestInWelsh)
        val document = Jsoup.parse(contentAsString(result))
        document.selectXpath("//*[@id=\"main-content\"]/div/div/p[2]").text() shouldBe "Mae’n bosibl bod y taliad wedi methi oherwydd:"
      }

      "render the page with the correct line 2 in Welsh" in {
        val result = systemUnderTest.renderPage(fakeGetRequestInWelsh)
        val document = Jsoup.parse(contentAsString(result))
        document.selectXpath("//*[@id=\"main-content\"]/div/div/ul/li[1]").text() shouldBe "nid oes yna ddigon o arian yn eich cyfrif"
      }

      "render the page with the correct line 3 in Welsh" in {
        val result = systemUnderTest.renderPage(fakeGetRequestInWelsh)
        val document = Jsoup.parse(contentAsString(result))
        document.selectXpath("//*[@id=\"main-content\"]/div/div/ul/li[2]").text() shouldBe "rydych wedi nodi manylion cerdyn sy’n annilys neu sydd wedi dod i ben"
      }

      "render the page with the correct line 4 in Welsh" in {
        val result = systemUnderTest.renderPage(fakeGetRequestInWelsh)
        val document = Jsoup.parse(contentAsString(result))
        document.selectXpath("//*[@id=\"main-content\"]/div/div/ul/li[3]").text() shouldBe "nid yw’r cyfeiriad a roesoch i ni’n cyd-fynd â’r un sydd gan ddosbarthwr eich cerdyn"
      }

    }

    "GET /payment-failed with Open Banking available as a Payment Method" - {

      val fakeGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/payment-failed")
      val fakeGetRequestInWelsh: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/payment-failed").withCookies(Cookie("PLAY_LANG", "cy"))

      "should return 200 OK" in {
        val result = systemUnderTest.renderPage()(fakeGetRequest)
        status(result) shouldBe Status.OK
      }

      "render the page with the correct sub heading in English" in {
        val result = systemUnderTest.renderPage(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        document.selectXpath("//*[@id=\"main-content\"]/div/div/p[1]").text() shouldBe "No payment has been taken from your card."
      }

      "render the page with the correct Radio Heading content in English" in {
        val result = systemUnderTest.renderPage(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        document.select(".govuk-fieldset__legend").text() shouldBe "What do you want to do?"
      }

      "render the page with the correct Radio contents in the correct order in English" in {

        val expectedContent = List("Approve a payment to come straight from my bank account", "Try card payment again")

        val result = systemUnderTest.renderPage(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        val radios = document.select(".govuk-radios__item").asScala.toList

        radios.map(_.text()) should contain theSameElementsInOrderAs expectedContent

      }

      "render the page with the correct sub heading in Welsh" in {
        val result = systemUnderTest.renderPage(fakeGetRequestInWelsh)
        val document = Jsoup.parse(contentAsString(result))
        document.selectXpath("//*[@id=\"main-content\"]/div/div/p[1]").text() shouldBe "Nid oes taliad wedi’i dynnu o’ch cerdyn."
      }

      "render the page with the correct Radio Heading content in Welsh" in {
        val result = systemUnderTest.renderPage(fakeGetRequestInWelsh)
        val document = Jsoup.parse(contentAsString(result))
        document.select(".govuk-fieldset__legend").text() shouldBe "Beth hoffech chi ei wneud?"
      }

      "render the page with the correct Radio contents in the correct order in Welsh" in {

        val expectedContent = List("Cymeradwyo taliad i fynd yn syth o’m cyfrif banc", "Rhowch gynnig arall ar dalu drwy gerdyn")

        val result = systemUnderTest.renderPage(fakeGetRequestInWelsh)
        val document = Jsoup.parse(contentAsString(result))
        val radios = document.select(".govuk-radios__item").asScala.toList

        radios.map(_.text()) should contain theSameElementsInOrderAs expectedContent

      }

    }

  }

}
