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

  "GET /card-fees" - {
    val fakeGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/cya0") //PfSa No email
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
  }
}
