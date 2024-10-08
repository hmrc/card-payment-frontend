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
import play.api.test.Helpers.status
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec
import play.api.test.Helpers._

import scala.jdk.CollectionConverters.CollectionHasAsScala

class FeesControllerSpec extends ItSpec {
  private val systemUnderTest: FeesController = app.injector.instanceOf[FeesController]

  "FeesController" - {
    "GET /card-fees" - {
      val fakeGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/card-fees0")
      val fakeGetRequestInWelsh: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/email-address").withCookies(Cookie("PLAY_LANG", "cy"))

      "should return 200 OK" in {
        val result = systemUnderTest.renderPage0()(fakeGetRequest)
        status(result) shouldBe Status.OK
      }

      "render the page with the hmrc layout" in {
        val result = systemUnderTest.renderPage0()(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        document.select("html").hasClass("govuk-template") shouldBe true withClue "no govuk template"
      }

      "render the page with the h1 correctly in English" in {
        val result = systemUnderTest.renderPage0()(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        document.select("h1").text() shouldBe "Card fees" withClue "Card fees page H1 wrong"
      }

      "render the page with the h1 correctly in Welsh" in {
        val result = systemUnderTest.renderPage0()(fakeGetRequestInWelsh)
        val document = Jsoup.parse(contentAsString(result))
        document.select("h1").text() shouldBe "Ffioedd cerdyn" withClue "Card fees Welsh page H1 wrong"
      }

      "render the page with the language toggle" in {
        val result = systemUnderTest.renderPage0()(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        val langToggleText: List[String] = document.select(".hmrc-language-select__list-item").eachText().asScala.toList
        langToggleText should contain theSameElementsAs List("English", "Newid yr iaith ir Gymraeg Cymraeg") //checking the visually hidden text, it's simpler
      }

      "render the page with a back button" in {
        val result = systemUnderTest.renderPage0()(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        val backButton = document.select(".govuk-back-link")
        backButton.text() shouldBe "Back"
        backButton.attr("href") shouldBe "#"
      }

      "render the page with a back button in welsh" in {
        val result = systemUnderTest.renderPage0()(fakeGetRequestInWelsh)
        val document = Jsoup.parse(contentAsString(result))
        val backButton = document.select(".govuk-back-link")
        backButton.text() shouldBe "Yn ôl"
        backButton.attr("href") shouldBe "#"
      }

      "render card fee para 1 in English" in {
        val result = systemUnderTest.renderPage0()(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        val para1 = document.select("#para1")
        para1.text() shouldBe "There is a non-refundable fee if you pay by corporate credit card or corporate debit card."
      }

      "render card fee para 2 in English" in {
        val result = systemUnderTest.renderPage0()(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        val para2 = document.select("#para2")
        para2.text() shouldBe "There is no fee if you pay by:"
      }

      "render card fee para 3 in English" in {
        val result = systemUnderTest.renderPage0()(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        val para3 = document.select("#para3")
        para3.text() shouldBe "You cannot pay using a personal credit card."
      }

      "render card fee para 4 in English" in {
        val result = systemUnderTest.renderPage0()(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        val para4 = document.select("#para4")
        para4.text() shouldBe "Allow 3 working days for your payment to reach HMRC’s bank account."
      }

      "render card fee para 1 in Welsh" in {
        val result = systemUnderTest.renderPage0()(fakeGetRequestInWelsh)
        val document = Jsoup.parse(contentAsString(result))
        val para1 = document.select("#para1")
        para1.text() shouldBe "Bydd ffi na ellir ei had-dalu yn cael ei chodi os talwch â cherdyn credyd corfforaethol neu gerdyn debyd corfforaethol."
      }

      "render card fee para 2 in Welsh" in {
        val result = systemUnderTest.renderPage0()(fakeGetRequestInWelsh)
        val document = Jsoup.parse(contentAsString(result))
        val para2 = document.select("#para2")
        para2.text() shouldBe "Nid oes ffi yn cael ei chodi os talwch drwy un o’r dulliau canlynol:"
      }

      "render card fee para 3 in Welsh" in {
        val result = systemUnderTest.renderPage0()(fakeGetRequestInWelsh)
        val document = Jsoup.parse(contentAsString(result))
        val para3 = document.select("#para3")
        para3.text() shouldBe "Ni allwch dalu â cherdyn credyd personol."
      }

      "render card fee para 4 in Welsh" in {
        val result = systemUnderTest.renderPage0()(fakeGetRequestInWelsh)
        val document = Jsoup.parse(contentAsString(result))
        val para4 = document.select("#para4")
        para4.text() shouldBe "Dylech ganiatáu 3 diwrnod gwaith i’ch taliad gyrraedd cyfrif banc CThEM."
      }

      "when open banking is not allowed (example render0) there is no open banking content" in {
        val result = systemUnderTest.renderPage0()(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        val bankAccountLink = document.select("#bank-account-link")
        bankAccountLink.isEmpty shouldBe true
      }

      "when open banking is allowed (example render1) there is open-banking content" in {
        val fakeGetRequest1: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/card-fees1")
        val result = systemUnderTest.renderPage1()(fakeGetRequest1)
        val document = Jsoup.parse(contentAsString(result))
        val bankAccountLink = document.select("#bank-account-link")
        bankAccountLink.text() shouldBe "bank account"
      }

      "when variable direct debit is allowed and one off direct debit is not allowed (example render2) there is variable direct debit content" in {
        val fakeGetRequest2: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/card-fees2")
        val result = systemUnderTest.renderPage2()(fakeGetRequest2)
        val document = Jsoup.parse(contentAsString(result))
        val variableDirectDebitLink = document.select("#ddlink-1")
        variableDirectDebitLink.text() shouldBe "direct debit"

      }

      "when variable direct debit is not allowed and one off direct debit is allowed (example render3) there is variable direct debit content" in {
        val fakeGetRequest3: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/card-fees3")
        val result = systemUnderTest.renderPage3()(fakeGetRequest3)
        val document = Jsoup.parse(contentAsString(result))
        val variableDirectDebitLink = document.select("#ddlink-1")
        variableDirectDebitLink.text() shouldBe "direct debit"
      }

      "when both variable direct debit is allowed and one off direct debit is allowed (example render4) and there is a primary link there is variable direct debit content" in {
        val fakeGetRequest4: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/card-fees4")
        val result = systemUnderTest.renderPage4()(fakeGetRequest4)
        val document = Jsoup.parse(contentAsString(result))
        val variableDirectDebitLink = document.select("#ddlink-1-both-primary")
        variableDirectDebitLink.text() shouldBe "direct debit"
      }

      "when both variable direct debit is allowed and one off direct debit is allowed (example render5) and there is a secondary link there is variable direct debit content" in {
        val fakeGetRequest5: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/card-fees5")
        val result = systemUnderTest.renderPage5()(fakeGetRequest5)
        val document = Jsoup.parse(contentAsString(result))
        val variableDirectDebitLink = document.select("#ddlink-1-both-secondary")
        variableDirectDebitLink.text() shouldBe "alt direct debit"
      }

    }
  }
}
