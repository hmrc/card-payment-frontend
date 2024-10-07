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
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import play.api.mvc.{AnyContentAsEmpty, Cookie}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.mvc.Http.Status
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec

import scala.jdk.CollectionConverters.ListHasAsScala

class PaymentCompleteControllerSpec extends ItSpec {

  "PaymentCompleteControllerSpec" - {

    val systemUnderTest = app.injector.instanceOf[PaymentCompleteController]

    "GET /payment-complete" - {

      val fakeGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/payment-complete")
      val fakeGetRequestInWelsh: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/payment-complete").withCookies(Cookie("PLAY_LANG", "cy"))

      "should return 200 OK" in {
        val result = systemUnderTest.renderPage(fakeGetRequest)
        status(result) shouldBe Status.OK
      }

      "render the page with the language toggle" in {
        val result = systemUnderTest.renderPage(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        val langToggleText: List[String] = document.select(".hmrc-language-select__list-item").eachText().asScala.toList
        langToggleText should contain theSameElementsAs List("English", "Newid yr iaith ir Gymraeg Cymraeg") //checking the visually hidden text, it's simpler
      }

      "render the page without a back link" in {
        val result = systemUnderTest.renderPage(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        val backLink: Elements = document.select(".govuk-back-link")
        backLink.size() shouldBe 0
      }

      "render the h1 panel correctly" in {
        val result = systemUnderTest.renderPage(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        val panel = document.body().select(".govuk-panel--confirmation")
        panel.select("h1").text() shouldBe "Payment received by HMRC"
        panel.select(".govuk-panel__body").html() shouldBe "Your payment reference\n<br><strong>1234567895K</strong>"
      }

      "render the h1 panel correctly in welsh" in {
        val result = systemUnderTest.renderPage(fakeGetRequestInWelsh)
        val document = Jsoup.parse(contentAsString(result))
        val panel = document.body().select(".govuk-panel--confirmation")
        panel.select("h1").text() shouldBe "Taliad wedi dod i law CThEM"
        panel.select(".govuk-panel__body").html() shouldBe "Eich cyfeirnod talu\n<br><strong>1234567895K</strong>"
      }

      "render paragraph about email address when email is provided" in {
        val result = systemUnderTest.renderPage(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        document.select("#email-paragraph").html() shouldBe "We have sent a confirmation email to <strong>blah@blah.com</strong>"
      }

      "render paragraph about email address in welsh when email is provided" in {
        val result = systemUnderTest.renderPage(fakeGetRequestInWelsh)
        val document = Jsoup.parse(contentAsString(result))
        document.select("#email-paragraph").html() shouldBe "Rydym wedi anfon e-bost cadarnhau <strong>blah@blah.com</strong>"
      }

      //can't implement yet until we define action refiners etc.
      "not render paragraph about email address when email is not provided" in {
        pending
      }

      /*
      todo enhance this test to test this page for EVERY origin, some origins have bespoke behaviour/content.
      use a seq or table_driven_property_checks from scalatest and have (taxType: String, expectedSummaryRows) => ... the test that asserts the predefined expected test results
       */
      "render the summary list correctly" in {
        val result = systemUnderTest.renderPage(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        val summaryListRows: List[Element] = document.select(".govuk-summary-list__row").asScala.toList
        val keyValuePairsOfSummaryRows: List[(String, String)] =
          summaryListRows.map(row => row.select(".govuk-summary-list__key").text() -> row.select(".govuk-summary-list__value").text())
        val expectedSummaryListRows: List[(String, String)] = List(
          "Tax" -> "Self assessment",
          "Date" -> "7 October 2024",
          "Amount" -> "£12.34"
        )

        keyValuePairsOfSummaryRows should contain theSameElementsInOrderAs (expectedSummaryListRows)
      }

      //todo enhance this test to test this page for EVERY origin, some origins have bespoke behaviour/content.
      "render the summary list correctly in welsh" in {
        pending
        val result = systemUnderTest.renderPage(fakeGetRequestInWelsh)
        val document = Jsoup.parse(contentAsString(result))
        document.select("").html() shouldBe ""
      }

      "render the print link correctly" in {
        val result = systemUnderTest.renderPage(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        val printLinkParagraphWrapper = document.select("#print-link")
        printLinkParagraphWrapper.hasClass("govuk-!-display-none-print") shouldBe true
        val printLink = printLinkParagraphWrapper.select("a")
        printLink.hasClass("govuk-link") shouldBe true
        printLink.attr("href") shouldBe "javascript:window.print()"
        printLink.text() shouldBe "Print your payment confirmation"
      }

      "render the print link correctly in welsh" in {
        val result = systemUnderTest.renderPage(fakeGetRequestInWelsh)
        val document = Jsoup.parse(contentAsString(result))
        val printLinkParagraphWrapper = document.select("#print-link")
        printLinkParagraphWrapper.hasClass("govuk-!-display-none-print") shouldBe true
        val printLink = printLinkParagraphWrapper.select("a")
        printLink.hasClass("govuk-link") shouldBe true
        printLink.attr("href") shouldBe "javascript:window.print()"
        printLink.text() shouldBe "Argraffwch cadarnhad o’ch taliad"
      }

      "render the survey content correctly" in {
        val result = systemUnderTest.renderPage(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        val surveyWrapper = document.select("#survey-wrapper")
        surveyWrapper.hasClass("govuk-!-display-none-print") shouldBe true
        surveyWrapper.select("h2").text() shouldBe "Help us improve our services"
        surveyWrapper.select("#survey-content").text() shouldBe "We use your feedback to make our services better."
        surveyWrapper.select("#survey-link-wrapper").html() shouldBe """<a class="govuk-link" href="ADD_ME">Tell us what you think of this service</a> (takes 30 seconds)"""
      }

      "render the survey content correctly in welsh" in {
        val result = systemUnderTest.renderPage(fakeGetRequestInWelsh)
        val document = Jsoup.parse(contentAsString(result))
        val surveyWrapper = document.select("#survey-wrapper")
        surveyWrapper.hasClass("govuk-!-display-none-print") shouldBe true
        surveyWrapper.select("h2").text() shouldBe "Helpwch ni i wella ein gwasanaethau"
        surveyWrapper.select("#survey-content").text() shouldBe "Rydym yn defnyddio’ch adborth i wella ein gwasanaethau."
        surveyWrapper.select("#survey-link-wrapper").html() shouldBe """<a class="govuk-link" href="ADD_ME">Rhowch wybod i ni beth yw eich barn am y gwasanaeth hwn</a> (mae’n cymryd 30 eiliad)"""
      }
    }
  }

}
