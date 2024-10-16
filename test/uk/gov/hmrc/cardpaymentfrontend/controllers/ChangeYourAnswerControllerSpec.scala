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

class ChangeYourAnswerControllerSpec extends ItSpec {
  private val systemUnderTest: CheckYourAnswersController = app.injector.instanceOf[CheckYourAnswersController]

  "GET /cya{x}" - {
    val fakeGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/cya0") //PfSa No email
    val fakeGetRequestInWelsh: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/cya0").withCookies(Cookie("PLAY_LANG", "cy"))

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
      document.select("h1").text() shouldBe "Check your answers" withClue "Check your answers page H1 wrong"
    }

    "render the page with the h1 correctly in Welsh" in {
      val result = systemUnderTest.renderPage0()(fakeGetRequestInWelsh)
      val document = Jsoup.parse(contentAsString(result))
      document.select("h1").text() shouldBe "Gwirio’ch atebion" withClue "Check your answers Welsh page H1 wrong"
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

    "render the reference name of PfSa in English" in {
      val result = systemUnderTest.renderPage0()(fakeGetRequest)
      val document = Jsoup.parse(contentAsString(result))
      val textOfBody = document.select("body").text()
      textOfBody.contains("Unique Taxpayer Reference (UTR)") shouldBe true
    }

    "render the reference name of PfSa in Welsh" in {
      val result = systemUnderTest.renderPage0()(fakeGetRequestInWelsh)
      val document = Jsoup.parse(contentAsString(result))
      val textOfBody = document.select("body").text()
      textOfBody.contains("Cyfeirnod Unigryw y Trethdalwr (UTR)") shouldBe true
    }

    "render the reference value name of PfSa" in {
      val result = systemUnderTest.renderPage0()(fakeGetRequest)
      val document = Jsoup.parse(contentAsString(result))
      val textOfBody = document.select("body").text()
      textOfBody.contains("1097172564") shouldBe true
    }

    "render the reference change link text in English" in {
      val result = systemUnderTest.renderPage0()(fakeGetRequest)
      val document = Jsoup.parse(contentAsString(result))
      val changeLink = document.select("#pfsa-reference-change-link").text()
      changeLink shouldBe "Change"
    }

    "render the reference change link text in Welsh" in {
      val result = systemUnderTest.renderPage0()(fakeGetRequestInWelsh)
      val document = Jsoup.parse(contentAsString(result))
      val changeLink = document.select("#pfsa-reference-change-link").text()
      changeLink shouldBe "Newid"
    }

    "render the amount title of PfSa in English" in {
      val result = systemUnderTest.renderPage0()(fakeGetRequest)
      val document = Jsoup.parse(contentAsString(result))
      val textOfBody = document.select("body").text()
      textOfBody.contains("Amount") shouldBe true
    }

    "render the amount title of PfSa in Welsh" in {
      pending
      val result = systemUnderTest.renderPage0()(fakeGetRequestInWelsh)
      val document = Jsoup.parse(contentAsString(result))
      val textOfBody = document.select("body").text()
      textOfBody.contains("???") shouldBe true
    }

    "render the amount value of PfSa" in {
      val result = systemUnderTest.renderPage0()(fakeGetRequest)
      val document = Jsoup.parse(contentAsString(result))
      val textOfBody = document.select("body").text()
      textOfBody.contains("£600") shouldBe true
    }

    "render the amount change link text in English" in {
      val result = systemUnderTest.renderPage0()(fakeGetRequest)
      val document = Jsoup.parse(contentAsString(result))
      val changeLink = document.select("#pfsa-amount-change-link").text()
      changeLink shouldBe "Change"
    }

    "render the amount change link text in Welsh" in {
      val result = systemUnderTest.renderPage0()(fakeGetRequestInWelsh)
      val document = Jsoup.parse(contentAsString(result))
      val changeLink = document.select("#pfsa-amount-change-link").text()
      changeLink shouldBe "Newid"
    }

    "render the address name of PfSa in English" in {
      val result = systemUnderTest.renderPage0()(fakeGetRequest)
      val document = Jsoup.parse(contentAsString(result))
      val textOfBody = document.select("body").text()
      textOfBody.contains("Card billing address") shouldBe true
    }

    "render the address name of PfSa in Welsh" in {
      pending
      val result = systemUnderTest.renderPage0()(fakeGetRequestInWelsh)
      val document = Jsoup.parse(contentAsString(result))
      val textOfBody = document.select("body").text()
      textOfBody.contains("???") shouldBe true
    }

    "render the email address title of PfSa in English" in {
      val result = systemUnderTest.renderPage0()(fakeGetRequest)
      val document = Jsoup.parse(contentAsString(result))
      val textOfBody = document.select("body").text()
      textOfBody.contains("Email address") shouldBe true
    }

    "render the email address title of PfSa in Welsh" in {
      val result = systemUnderTest.renderPage0()(fakeGetRequestInWelsh)
      val document = Jsoup.parse(contentAsString(result))
      val textOfBody = document.select("body").text()
      textOfBody.contains("Cyfeiriad e-bost ar gyfer y dderbynneb") shouldBe true
    }

    "render the email supply link text in English" in {
      val result = systemUnderTest.renderPage0()(fakeGetRequest)
      val document = Jsoup.parse(contentAsString(result))
      val changeLink = document.select("#pfsa-email-supply-link").text()
      changeLink shouldBe "Enter email address"
    }

    "render the email supply link text in Welsh" in {
      pending
      val result = systemUnderTest.renderPage0()(fakeGetRequestInWelsh)
      val document = Jsoup.parse(contentAsString(result))
      val changeLink = document.select("#pfsa-email-supply-link").text()
      changeLink shouldBe "???"
    }

    "render a final reference line in English" in {
      val result = systemUnderTest.renderPage0()(fakeGetRequest)
      val document = Jsoup.parse(contentAsString(result))
      val changeLink = document.select("#cya-final-ref").text()
      changeLink shouldBe "This payment will show in your bank as 1097172564."
    }

    "render a final reference line in Welsh" in {
      val result = systemUnderTest.renderPage0()(fakeGetRequestInWelsh)
      val document = Jsoup.parse(contentAsString(result))
      val changeLink = document.select("#cya-final-ref").text()
      changeLink shouldBe "Bydd y taliad hwn yn dangos yn eich banc fel 1097172564."
    }
  }
}
