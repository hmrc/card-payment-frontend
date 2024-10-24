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
import payapi.cardpaymentjourney.model.journey.{Journey, JourneySpecificData}
import payapi.corcommon.model.JourneyId
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.mvc.Http.Status
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec
import uk.gov.hmrc.cardpaymentfrontend.testsupport.TestOps._
import uk.gov.hmrc.cardpaymentfrontend.testsupport.stubs.PayApiStub
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.TestJourneys

import scala.jdk.CollectionConverters.ListHasAsScala

class PaymentCompleteControllerSpec extends ItSpec {

  "PaymentCompleteController" - {

    val systemUnderTest = app.injector.instanceOf[PaymentCompleteController]

    "GET /payment-complete" - {

      val fakeGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/payment-complete").withSessionId()
      val fakeGetRequestInWelsh: FakeRequest[AnyContentAsEmpty.type] = fakeGetRequest.withLangWelsh()

      "render the page with the language toggle" in {
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.testPfSaJourneySuccessDebit)
        val result = systemUnderTest.renderPage(fakeGetRequest)
        status(result) shouldBe Status.OK
        val document = Jsoup.parse(contentAsString(result))
        val langToggleText: List[String] = document.select(".hmrc-language-select__list-item").eachText().asScala.toList
        langToggleText should contain theSameElementsAs List("English", "Newid yr iaith ir Gymraeg Cymraeg") //checking the visually hidden text, it's simpler
      }

      "render the page without a back link" in {
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.testPfSaJourneySuccessDebit)
        val result = systemUnderTest.renderPage(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        val backLink: Elements = document.select(".govuk-back-link")
        backLink.size() shouldBe 0
      }

      "render the h1 panel correctly" in {
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.testPfSaJourneySuccessDebit)
        val result = systemUnderTest.renderPage(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        val panel = document.body().select(".govuk-panel--confirmation")
        panel.select("h1").text() shouldBe "Payment received by HMRC"
        panel.select(".govuk-panel__body").html() shouldBe "Your payment reference\n<br><strong>1234567895K</strong>"
      }

      "render the h1 panel correctly in welsh" in {
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.testPfSaJourneySuccessDebit)
        val result = systemUnderTest.renderPage(fakeGetRequestInWelsh)
        val document = Jsoup.parse(contentAsString(result))
        val panel = document.body().select(".govuk-panel--confirmation")
        panel.select("h1").text() shouldBe "Taliad wedi dod i law CThEM"
        panel.select(".govuk-panel__body").html() shouldBe "Eich cyfeirnod talu\n<br><strong>1234567895K</strong>"
      }

      "render paragraph about email address when email is provided" in {
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.testPfSaJourneySuccessDebit)
        val requestForTest = fakeGetRequest.withEmailInSession(JourneyId("TestJourneyId-44f9-ad7f-01e1d3d8f151"))
        val result = systemUnderTest.renderPage(requestForTest)
        val document = Jsoup.parse(contentAsString(result))
        document.select("#email-paragraph").html() shouldBe "We have sent a confirmation email to <strong>blah@blah.com</strong>"
      }

      "render paragraph about email address in welsh when email is provided" in {
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.testPfSaJourneySuccessDebit)
        val requestForTest = fakeGetRequestInWelsh.withEmailInSession(JourneyId("TestJourneyId-44f9-ad7f-01e1d3d8f151"))
        val result = systemUnderTest.renderPage(requestForTest)
        val document = Jsoup.parse(contentAsString(result))
        document.select("#email-paragraph").html() shouldBe "Rydym wedi anfon e-bost cadarnhau <strong>blah@blah.com</strong>"
      }

      "not render paragraph about email address when email is not provided" in {
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.testPfSaJourneySuccessDebit)
        val result = systemUnderTest.renderPage(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        document.select("#email-paragraph").size() shouldBe 0
      }

      "render the print link correctly" in {
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.testPfSaJourneySuccessDebit)
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
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.testPfSaJourneySuccessDebit)
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
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.testPfSaJourneySuccessDebit)
        val result = systemUnderTest.renderPage(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        val surveyWrapper = document.select("#survey-wrapper")
        surveyWrapper.hasClass("govuk-!-display-none-print") shouldBe true
        surveyWrapper.select("h2").text() shouldBe "Help us improve our services"
        surveyWrapper.select("#survey-content").text() shouldBe "We use your feedback to make our services better."
        surveyWrapper.select("#survey-link-wrapper").html() shouldBe """<a class="govuk-link" href="ADD_ME">Tell us what you think of this service</a> (takes 30 seconds)"""
      }

      "render the survey content correctly in welsh" in {
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.testPfSaJourneySuccessDebit)
        val result = systemUnderTest.renderPage(fakeGetRequestInWelsh)
        val document = Jsoup.parse(contentAsString(result))
        val surveyWrapper = document.select("#survey-wrapper")
        surveyWrapper.hasClass("govuk-!-display-none-print") shouldBe true
        surveyWrapper.select("h2").text() shouldBe "Helpwch ni i wella ein gwasanaethau"
        surveyWrapper.select("#survey-content").text() shouldBe "Rydym yn defnyddio’ch adborth i wella ein gwasanaethau."
        surveyWrapper.select("#survey-link-wrapper").html() shouldBe """<a class="govuk-link" href="ADD_ME">Rhowch wybod i ni beth yw eich barn am y gwasanaeth hwn</a> (mae’n cymryd 30 eiliad)"""
      }

        def testSummaryRows(testData: Journey[JourneySpecificData], fakeRequest: FakeRequest[_], expectedSummaryListRows: List[(String, String)]) = {
          PayApiStub.stubForFindBySessionId2xx(testData)
          val result = systemUnderTest.renderPage(fakeRequest)
          val document = Jsoup.parse(contentAsString(result))
          val summaryListRows: List[Element] = document.select(".govuk-summary-list__row").asScala.toList
          val keyValuePairsOfSummaryRows: List[(String, String)] =
            summaryListRows.map(row => row.select(".govuk-summary-list__key").text() -> row.select(".govuk-summary-list__value").text())

          keyValuePairsOfSummaryRows should contain theSameElementsInOrderAs expectedSummaryListRows
        }

      "for origin PfSa" - {

        "when paying by debit card" - {

          "render the summary list correctly" in {
            val expectedSummaryListRows: List[(String, String)] = List(
              "Tax" -> "Self Assessment",
              "Date" -> "2 November 2027",
              "Amount" -> "£12.34"
            )
            testSummaryRows(TestJourneys.testPfSaJourneySuccessDebit, fakeGetRequest, expectedSummaryListRows)
          }

          "render the summary list correctly in welsh" in {
            val expectedSummaryListRows: List[(String, String)] = List(
              "Treth" -> "Hunanasesiad",
              "Dyddiad" -> "2 Tachwedd 2027",
              "Swm" -> "£12.34"
            )
            testSummaryRows(TestJourneys.testPfSaJourneySuccessDebit, fakeGetRequestInWelsh, expectedSummaryListRows)
          }
        }

        "when paying by a card that incurs a surcharge" - {

          "render the summary list correctly when payment has a surcharge" in {
            val expectedSummaryListRows: List[(String, String)] = List(
              "Tax" -> "Self Assessment",
              "Date" -> "2 November 2027",
              "Amount paid to HMRC" -> "£12.34",
              "Card fee (9.97%), non-refundable" -> "£1.23",
              "Total paid" -> "£13.57"
            )
            testSummaryRows(TestJourneys.testPfSaJourneySuccessCredit, fakeGetRequest, expectedSummaryListRows)
          }

          "render the summary list correctly in welsh when payment has a surcharge" in {
            val expectedSummaryListRows: List[(String, String)] = List(
              "Treth" -> "Hunanasesiad",
              "Dyddiad" -> "2 Tachwedd 2027",
              "Swm a dalwyd i CThEM" -> "£12.34",
              "Ffi cerdyn (9.97%), ni ellir ei ad-dalu" -> "£1.23",
              "Cyfanswm a dalwyd" -> "£13.57"
            )
            testSummaryRows(TestJourneys.testPfSaJourneySuccessCredit, fakeGetRequestInWelsh, expectedSummaryListRows)
          }
        }

      }

      "for origin BtaSa" - {

        "when paying by debit card" - {

          "render the summary list correctly" in {
            val expectedSummaryListRows: List[(String, String)] = List(
              "Tax" -> "Self Assessment",
              "Date" -> "2 November 2027",
              "Amount" -> "£12.34"
            )
            testSummaryRows(TestJourneys.testBtaSaJourneySuccessDebit, fakeGetRequest, expectedSummaryListRows)

            /**
             * TODO: Mike, we may need to assert custom content for 'logged in' origins i.e. bta/pta, e.g:
             *
             * What happens next
             * Your payment will take 3 to 5 days to show in your HMRC online account. <-- there's a href here we should assert too.
             *
             */

          }

          "render the summary list correctly in welsh" in {
            val expectedSummaryListRows: List[(String, String)] = List(
              "Treth" -> "Hunanasesiad",
              "Dyddiad" -> "2 Tachwedd 2027",
              "Swm" -> "£12.34"
            )
            testSummaryRows(TestJourneys.testBtaSaJourneySuccessDebit, fakeGetRequestInWelsh, expectedSummaryListRows)
          }
        }

        "when paying by a card that incurs a surcharge" - {

          "render the summary list correctly when payment has a surcharge" in {
            val expectedSummaryListRows: List[(String, String)] = List(
              "Tax" -> "Self Assessment",
              "Date" -> "2 November 2027",
              "Amount paid to HMRC" -> "£12.34",
              "Card fee (9.97%), non-refundable" -> "£1.23",
              "Total paid" -> "£13.57"
            )
            testSummaryRows(TestJourneys.testBtaSaJourneySuccessCredit, fakeGetRequest, expectedSummaryListRows)
          }

          "render the summary list correctly in welsh when payment has a surcharge" in {
            val expectedSummaryListRows: List[(String, String)] = List(
              "Treth" -> "Hunanasesiad",
              "Dyddiad" -> "2 Tachwedd 2027",
              "Swm a dalwyd i CThEM" -> "£12.34",
              "Ffi cerdyn (9.97%), ni ellir ei ad-dalu" -> "£1.23",
              "Cyfanswm a dalwyd" -> "£13.57"
            )
            testSummaryRows(TestJourneys.testBtaSaJourneySuccessCredit, fakeGetRequestInWelsh, expectedSummaryListRows)
          }
        }
      }

      "for origin PtaSa" - {

        "when paying by debit card" - {

          "render the summary list correctly" in {
            val expectedSummaryListRows: List[(String, String)] = List(
              "Tax" -> "Self Assessment",
              "Date" -> "2 November 2027",
              "Amount" -> "£12.34"
            )
            testSummaryRows(TestJourneys.testPtaSaJourneySuccessDebit, fakeGetRequest, expectedSummaryListRows)

            /**
             * TODO: Mike, we may need to assert custom content for 'logged in' origins i.e. bta/pta, e.g:
             *
             * What happens next
             * Your payment will take 3 to 5 days to show in your HMRC online account. <-- there's a href here we should assert too.
             *
             */

          }

          "render the summary list correctly in welsh" in {
            val expectedSummaryListRows: List[(String, String)] = List(
              "Treth" -> "Hunanasesiad",
              "Dyddiad" -> "2 Tachwedd 2027",
              "Swm" -> "£12.34"
            )
            testSummaryRows(TestJourneys.testPtaSaJourneySuccessDebit, fakeGetRequestInWelsh, expectedSummaryListRows)
          }
        }

        "when paying by a card that incurs a surcharge" - {

          "render the summary list correctly when payment has a surcharge" in {
            val expectedSummaryListRows: List[(String, String)] = List(
              "Tax" -> "Self Assessment",
              "Date" -> "2 November 2027",
              "Amount paid to HMRC" -> "£12.34",
              "Card fee (9.97%), non-refundable" -> "£1.23",
              "Total paid" -> "£13.57"
            )
            testSummaryRows(TestJourneys.testPtaSaJourneySuccessCredit, fakeGetRequest, expectedSummaryListRows)
          }

          "render the summary list correctly in welsh when payment has a surcharge" in {
            val expectedSummaryListRows: List[(String, String)] = List(
              "Treth" -> "Hunanasesiad",
              "Dyddiad" -> "2 Tachwedd 2027",
              "Swm a dalwyd i CThEM" -> "£12.34",
              "Ffi cerdyn (9.97%), ni ellir ei ad-dalu" -> "£1.23",
              "Cyfanswm a dalwyd" -> "£13.57"
            )
            testSummaryRows(TestJourneys.testPtaSaJourneySuccessCredit, fakeGetRequestInWelsh, expectedSummaryListRows)
          }
        }
      }

      //not sure if this is truly needed.
      "for origin Itsa" - {

      }
    }
  }

}
