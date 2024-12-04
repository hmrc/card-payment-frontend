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
import payapi.cardpaymentjourney.model.barclays.BarclaysOrder
import payapi.cardpaymentjourney.model.journey.{Journey, JourneySpecificData, Url}
import payapi.corcommon.model.barclays.{CardCategories, TransactionReference}
import payapi.corcommon.model.{AmountInPence, JourneyId}
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.mvc.Http.Status
import uk.gov.hmrc.cardpaymentfrontend.models.EmailAddress
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec
import uk.gov.hmrc.cardpaymentfrontend.testsupport.TestOps._
import uk.gov.hmrc.cardpaymentfrontend.testsupport.stubs.{EmailStub, PayApiStub}
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.TestJourneys
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}

import java.time.LocalDateTime
import scala.jdk.CollectionConverters.ListHasAsScala

class PaymentCompleteControllerSpec extends ItSpec {

  "PaymentCompleteController" - {

    val systemUnderTest = app.injector.instanceOf[PaymentCompleteController]
    val fakeGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/payment-complete").withSessionId()
    val fakeGetRequestInWelsh: FakeRequest[AnyContentAsEmpty.type] = fakeGetRequest.withLangWelsh()

    "GET /payment-complete" - {

      val jsonBody = Json.parse(

        """
        {
          "to" : [ "blah@blah.com" ],
          "templateId" : "payment_successful",
          "parameters" : {
            "taxType" : "Self Assessment",
            "taxReference" : "1234567895K",
            "paymentReference" : "Some-transaction-ref",
            "amountPaid" : "12.34",
            "totalPaid" : "12.34"
          },
          "force" : false
        }
        """
      )

      "render the page with the language toggle" in {
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.testPfSaJourneySuccessDebit)
        val result = systemUnderTest.renderPage(fakeGetRequest)
        status(result) shouldBe Status.OK
        val document = Jsoup.parse(contentAsString(result))
        val langToggleText: List[String] = document.select(".hmrc-language-select__list-item").eachText().asScala.toList
        langToggleText should contain theSameElementsAs List("English", "Newid yr iaith ir Gymraeg Cymraeg") //checking the visually hidden text, it's simpler
      }

      "render the page without a back link" in {
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.testPfSaJourneySuccessDebit)
        val result = systemUnderTest.renderPage(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        val backLink: Elements = document.select(".govuk-back-link")
        backLink.size() shouldBe 0
      }

      "render the h1 panel correctly" in {
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.testPfSaJourneySuccessDebit)
        val result = systemUnderTest.renderPage(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        val panel = document.body().select(".govuk-panel--confirmation")
        panel.select("h1").text() shouldBe "Payment received by HMRC"
        panel.select(".govuk-panel__body").html() shouldBe "Your payment reference\n<br><strong>1234567895K</strong>"
      }

      "render the h1 panel correctly in welsh" in {
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.testPfSaJourneySuccessDebit)
        val result = systemUnderTest.renderPage(fakeGetRequestInWelsh)
        val document = Jsoup.parse(contentAsString(result))
        val panel = document.body().select(".govuk-panel--confirmation")
        panel.select("h1").text() shouldBe "Taliad wedi dod i law CThEM"
        panel.select(".govuk-panel__body").html() shouldBe "Eich cyfeirnod talu\n<br><strong>1234567895K</strong>"
      }

      "render paragraph about email address when email is provided" in {
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.testPfSaJourneySuccessDebit)
        val requestForTest = fakeGetRequest.withEmailInSession(JourneyId("TestJourneyId-44f9-ad7f-01e1d3d8f151"))
        val result = systemUnderTest.renderPage(requestForTest)
        val document = Jsoup.parse(contentAsString(result))
        document.select("#email-paragraph").html() shouldBe "We have sent a confirmation email to <strong>blah@blah.com</strong>"
      }

      "render paragraph about email address in welsh when email is provided" in {
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.testPfSaJourneySuccessDebit)
        val requestForTest = fakeGetRequestInWelsh.withEmailInSession(JourneyId("TestJourneyId-44f9-ad7f-01e1d3d8f151"))
        val result = systemUnderTest.renderPage(requestForTest)
        val document = Jsoup.parse(contentAsString(result))
        document.select("#email-paragraph").html() shouldBe "Rydym wedi anfon e-bost cadarnhau <strong>blah@blah.com</strong>"
      }

      "not render paragraph about email address when email is not provided" in {
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.testPfSaJourneySuccessDebit)
        val result = systemUnderTest.renderPage(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        document.select("#email-paragraph").size() shouldBe 0
      }

      "render the print link correctly" in {
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.testPfSaJourneySuccessDebit)
        val result = systemUnderTest.renderPage(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        val printLinkWrapper = document.select("#print-link-wrapper")
        printLinkWrapper.hasClass("govuk-!-display-none-print") shouldBe true
        printLinkWrapper.hasClass("js-visible") shouldBe true
        val printLink = printLinkWrapper.select("a")
        printLink.hasClass("govuk-link") shouldBe true
        printLink.attr("href") shouldBe "#print-dialogue"
        printLink.text() shouldBe "Print your payment confirmation"
      }

      "render the print link correctly in welsh" in {
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.testPfSaJourneySuccessDebit)
        val result = systemUnderTest.renderPage(fakeGetRequestInWelsh)
        val document = Jsoup.parse(contentAsString(result))
        val printLinkWrapper = document.select("#print-link-wrapper")
        printLinkWrapper.hasClass("govuk-!-display-none-print") shouldBe true
        printLinkWrapper.hasClass("js-visible") shouldBe true
        val printLink = printLinkWrapper.select("a")
        printLink.hasClass("govuk-link") shouldBe true
        printLink.attr("href") shouldBe "#print-dialogue"
        printLink.text() shouldBe "Argraffwch cadarnhad o’ch taliad"
      }

      "render the survey content correctly" in {
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.testPfSaJourneySuccessDebit)
        val result = systemUnderTest.renderPage(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        val surveyWrapper = document.select("#survey-wrapper")
        surveyWrapper.hasClass("govuk-!-display-none-print") shouldBe true
        surveyWrapper.select("h2").text() shouldBe "Help us improve our services"
        surveyWrapper.select("#survey-content").text() shouldBe "We use your feedback to make our services better."
        surveyWrapper.select("#survey-link-wrapper").html() shouldBe """<a class="govuk-link" href="/pay-by-card/start-payment-survey">Tell us what you think of this service</a> (takes 30 seconds)"""
      }

      "render the survey content correctly in welsh" in {
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.testPfSaJourneySuccessDebit)
        val result = systemUnderTest.renderPage(fakeGetRequestInWelsh)
        val document = Jsoup.parse(contentAsString(result))
        val surveyWrapper = document.select("#survey-wrapper")
        surveyWrapper.hasClass("govuk-!-display-none-print") shouldBe true
        surveyWrapper.select("h2").text() shouldBe "Helpwch ni i wella ein gwasanaethau"
        surveyWrapper.select("#survey-content").text() shouldBe "Rydym yn defnyddio’ch adborth i wella ein gwasanaethau."
        surveyWrapper.select("#survey-link-wrapper").html() shouldBe """<a class="govuk-link" href="/pay-by-card/start-payment-survey">Rhowch wybod i ni beth yw eich barn am y gwasanaeth hwn</a> (mae’n cymryd 30 eiliad)"""
      }

      //this test will be moved eventually
      "should send an email if there is one in the session" in {
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.testPfSaJourneySuccessDebit)
        val fakeRequestForTest = fakeGetRequest.withEmailInSession(TestJourneys.PfSa.testPfSaJourneySuccessDebit._id, EmailAddress("blah@blah.com"))
        val result = systemUnderTest.renderPage(fakeRequestForTest)
        status(result) shouldBe Status.OK
        EmailStub.verifyEmailWasSent(jsonBody)
      }

      "should not send an email if there is not one in the session" in {
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.testPfSaJourneySuccessDebit)
        val result = systemUnderTest.renderPage(fakeGetRequest)
        status(result) shouldBe Status.OK
        EmailStub.verifyEmailWasNotSend()
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
            testSummaryRows(TestJourneys.PfSa.testPfSaJourneySuccessDebit, fakeGetRequest, expectedSummaryListRows)
          }

          "render the summary list correctly in welsh" in {
            val expectedSummaryListRows: List[(String, String)] = List(
              "Treth" -> "Hunanasesiad",
              "Dyddiad" -> "2 Tachwedd 2027",
              "Swm" -> "£12.34"
            )
            testSummaryRows(TestJourneys.PfSa.testPfSaJourneySuccessDebit, fakeGetRequestInWelsh, expectedSummaryListRows)
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
            testSummaryRows(TestJourneys.PfSa.testPfSaJourneySuccessCredit, fakeGetRequest, expectedSummaryListRows)
          }

          "render the summary list correctly in welsh when payment has a surcharge" in {
            val expectedSummaryListRows: List[(String, String)] = List(
              "Treth" -> "Hunanasesiad",
              "Dyddiad" -> "2 Tachwedd 2027",
              "Swm a dalwyd i CThEM" -> "£12.34",
              "Ffi cerdyn (9.97%), ni ellir ei ad-dalu" -> "£1.23",
              "Cyfanswm a dalwyd" -> "£13.57"
            )
            testSummaryRows(TestJourneys.PfSa.testPfSaJourneySuccessCredit, fakeGetRequestInWelsh, expectedSummaryListRows)
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
            testSummaryRows(TestJourneys.BtaSa.testBtaSaJourneySuccessDebit, fakeGetRequest, expectedSummaryListRows)
          }

          "render the summary list correctly in welsh" in {
            val expectedSummaryListRows: List[(String, String)] = List(
              "Treth" -> "Hunanasesiad",
              "Dyddiad" -> "2 Tachwedd 2027",
              "Swm" -> "£12.34"
            )
            testSummaryRows(TestJourneys.BtaSa.testBtaSaJourneySuccessDebit, fakeGetRequestInWelsh, expectedSummaryListRows)
          }

          "render the custom what happens next content" in {
            PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaSa.testBtaSaJourneySuccessDebit)
            val result = systemUnderTest.renderPage(fakeGetRequest)
            val document = Jsoup.parse(contentAsString(result))
            val wrapper = document.select("#what-happens-next-wrapper")
            wrapper.select("h4").text() shouldBe "What happens next"
            wrapper.select("p").html() shouldBe "Your payment will take 3 to 5 days to show in your <a class=\"govuk-link\" href=\"https://www.return-to-bta.com\">HMRC online account.</a>"
          }

          "render the custom what happens next content in welsh" in {
            PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaSa.testBtaSaJourneySuccessDebit)
            val result = systemUnderTest.renderPage(fakeGetRequestInWelsh)
            val document = Jsoup.parse(contentAsString(result))
            val wrapper = document.select("#what-happens-next-wrapper")
            wrapper.select("h4").text() shouldBe "Yr hyn sy’n digwydd nesaf"
            wrapper.select("p").html() shouldBe "Bydd eich taliad yn cymryd 3 i 5 diwrnod i ymddangos yn eich <a class=\"govuk-link\" href=\"https://www.return-to-bta.com\">cyfrif CThEM ar-lein.</a>"
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
            testSummaryRows(TestJourneys.BtaSa.testBtaSaJourneySuccessCredit, fakeGetRequest, expectedSummaryListRows)
          }

          "render the summary list correctly in welsh when payment has a surcharge" in {
            val expectedSummaryListRows: List[(String, String)] = List(
              "Treth" -> "Hunanasesiad",
              "Dyddiad" -> "2 Tachwedd 2027",
              "Swm a dalwyd i CThEM" -> "£12.34",
              "Ffi cerdyn (9.97%), ni ellir ei ad-dalu" -> "£1.23",
              "Cyfanswm a dalwyd" -> "£13.57"
            )
            testSummaryRows(TestJourneys.BtaSa.testBtaSaJourneySuccessCredit, fakeGetRequestInWelsh, expectedSummaryListRows)
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
            testSummaryRows(TestJourneys.PtaSa.testPtaSaJourneySuccessDebit, fakeGetRequest, expectedSummaryListRows)

          }

          "render the summary list correctly in welsh" in {
            val expectedSummaryListRows: List[(String, String)] = List(
              "Treth" -> "Hunanasesiad",
              "Dyddiad" -> "2 Tachwedd 2027",
              "Swm" -> "£12.34"
            )
            testSummaryRows(TestJourneys.PtaSa.testPtaSaJourneySuccessDebit, fakeGetRequestInWelsh, expectedSummaryListRows)
          }

          "render the custom what happens next content" in {
            PayApiStub.stubForFindBySessionId2xx(TestJourneys.PtaSa.testPtaSaJourneySuccessDebit)
            val result = systemUnderTest.renderPage(fakeGetRequest)
            val document = Jsoup.parse(contentAsString(result))
            val wrapper = document.select("#what-happens-next-wrapper")
            wrapper.select("h4").text() shouldBe "What happens next"
            wrapper.select("p").html() shouldBe "Your payment will take 3 to 5 days to show in your <a class=\"govuk-link\" href=\"https://www.return-to-pta.com\">HMRC online account.</a>"
          }

          "render the custom what happens next content in welsh" in {
            PayApiStub.stubForFindBySessionId2xx(TestJourneys.PtaSa.testPtaSaJourneySuccessDebit)
            val result = systemUnderTest.renderPage(fakeGetRequestInWelsh)
            val document = Jsoup.parse(contentAsString(result))
            val wrapper = document.select("#what-happens-next-wrapper")
            wrapper.select("h4").text() shouldBe "Yr hyn sy’n digwydd nesaf"
            wrapper.select("p").html() shouldBe "Bydd eich taliad yn cymryd 3 i 5 diwrnod i ymddangos yn eich <a class=\"govuk-link\" href=\"https://www.return-to-pta.com\">cyfrif CThEM ar-lein.</a>"
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
            testSummaryRows(TestJourneys.PtaSa.testPtaSaJourneySuccessCredit, fakeGetRequest, expectedSummaryListRows)
          }

          "render the summary list correctly in welsh when payment has a surcharge" in {
            val expectedSummaryListRows: List[(String, String)] = List(
              "Treth" -> "Hunanasesiad",
              "Dyddiad" -> "2 Tachwedd 2027",
              "Swm a dalwyd i CThEM" -> "£12.34",
              "Ffi cerdyn (9.97%), ni ellir ei ad-dalu" -> "£1.23",
              "Cyfanswm a dalwyd" -> "£13.57"
            )
            testSummaryRows(TestJourneys.PtaSa.testPtaSaJourneySuccessCredit, fakeGetRequestInWelsh, expectedSummaryListRows)
          }
        }
      }

      "for origin Itsa" - {
        "when paying by debit card" - {

          "render the summary list correctly" in {
            val expectedSummaryListRows: List[(String, String)] = List(
              "Tax" -> "Self Assessment",
              "Date" -> "2 November 2027",
              "Amount" -> "£12.34"
            )
            testSummaryRows(TestJourneys.ItSa.testItSaJourneySuccessDebit, fakeGetRequest, expectedSummaryListRows)

          }

          "render the summary list correctly in welsh" in {
            val expectedSummaryListRows: List[(String, String)] = List(
              "Treth" -> "Hunanasesiad",
              "Dyddiad" -> "2 Tachwedd 2027",
              "Swm" -> "£12.34"
            )
            testSummaryRows(TestJourneys.ItSa.testItSaJourneySuccessDebit, fakeGetRequestInWelsh, expectedSummaryListRows)
          }

          "render the custom what happens next content" in {
            PayApiStub.stubForFindBySessionId2xx(TestJourneys.ItSa.testItSaJourneySuccessDebit)
            val result = systemUnderTest.renderPage(fakeGetRequest)
            val document = Jsoup.parse(contentAsString(result))
            val wrapper = document.select("#what-happens-next-wrapper")
            wrapper.select("h4").text() shouldBe "What happens next"
            wrapper.select("p").html() shouldBe "Your payment will take 3 to 5 days to show in your <a class=\"govuk-link\" href=\"https://www.return-to-itsa.com\">HMRC online account.</a>"
          }

          "render the custom what happens next content in welsh" in {
            PayApiStub.stubForFindBySessionId2xx(TestJourneys.ItSa.testItSaJourneySuccessDebit)
            val result = systemUnderTest.renderPage(fakeGetRequestInWelsh)
            val document = Jsoup.parse(contentAsString(result))
            val wrapper = document.select("#what-happens-next-wrapper")
            wrapper.select("h4").text() shouldBe "Yr hyn sy’n digwydd nesaf"
            wrapper.select("p").html() shouldBe "Bydd eich taliad yn cymryd 3 i 5 diwrnod i ymddangos yn eich <a class=\"govuk-link\" href=\"https://www.return-to-itsa.com\">cyfrif CThEM ar-lein.</a>"
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
            testSummaryRows(TestJourneys.ItSa.testItSaJourneySuccessCredit, fakeGetRequest, expectedSummaryListRows)
          }

          "render the summary list correctly in welsh when payment has a surcharge" in {
            val expectedSummaryListRows: List[(String, String)] = List(
              "Treth" -> "Hunanasesiad",
              "Dyddiad" -> "2 Tachwedd 2027",
              "Swm a dalwyd i CThEM" -> "£12.34",
              "Ffi cerdyn (9.97%), ni ellir ei ad-dalu" -> "£1.23",
              "Cyfanswm a dalwyd" -> "£13.57"
            )
            testSummaryRows(TestJourneys.ItSa.testItSaJourneySuccessCredit, fakeGetRequestInWelsh, expectedSummaryListRows)
          }
        }
      }
    }

    "buildAmountsSummaryListRow" - {
        def messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
      "should return just the amount row when card type is debit" in {
        implicit val messages: Messages = messagesApi.preferred(fakeGetRequest)
        val summaryListRow: Seq[SummaryListRow] = PaymentCompleteController.buildAmountsSummaryListRow(TestJourneys.PfSa.testPfSaJourneySuccessDebit)
        summaryListRow shouldBe Seq(SummaryListRow(Key(Text("Amount"), ""), Value(Text("£12.34"), ""), "", None))
      }

      "should return just the amount row in welsh when card type is debit" in {
        implicit val messages: Messages = messagesApi.preferred(fakeGetRequestInWelsh)
        val summaryListRow: Seq[SummaryListRow] = PaymentCompleteController.buildAmountsSummaryListRow(TestJourneys.PfSa.testPfSaJourneySuccessDebit)
        summaryListRow shouldBe Seq(SummaryListRow(Key(Text("Swm"), ""), Value(Text("£12.34"), ""), "", None))
      }

      "should return just the amount row with the amount in GDS format (i.e. don't show £x.00 when pennies is 0)" in {
        implicit val messages: Messages = messagesApi.preferred(fakeGetRequest)
        val journey = TestJourneys.PfSa.testPfSaJourneySuccessDebit.copy(amountInPence = Some(AmountInPence(123000)))
        val summaryListRow: Seq[SummaryListRow] =
          PaymentCompleteController.buildAmountsSummaryListRow(journey)
        summaryListRow shouldBe Seq(SummaryListRow(Key(Text("Amount"), ""), Value(Text("£1,230"), ""), "", None))
      }

      "should return amount paid, card fee and total paid rows when card type is credit" in {
        implicit val messages: Messages = messagesApi.preferred(fakeGetRequest)
        val summaryListRows: Seq[SummaryListRow] = PaymentCompleteController.buildAmountsSummaryListRow(TestJourneys.PfSa.testPfSaJourneySuccessCredit)
        val expectedSummaryListRows = Seq(
          SummaryListRow(Key(Text("Amount paid to HMRC"), ""), Value(Text("£12.34"), ""), "", None),
          SummaryListRow(Key(HtmlContent("<nobr>Card fee (9.97%),</nobr><br/><nobr>non-refundable</nobr>"), ""), Value(Text("£1.23"), ""), "", None),
          SummaryListRow(Key(Text("Total paid"), ""), Value(Text("£13.57"), ""), "", None)
        )
        summaryListRows shouldBe expectedSummaryListRows
      }

      "should return amount paid, card fee and total paid rows in welsh when card type is credit" in {
        implicit val messages: Messages = messagesApi.preferred(fakeGetRequestInWelsh)
        val summaryListRows: Seq[SummaryListRow] = PaymentCompleteController.buildAmountsSummaryListRow(TestJourneys.PfSa.testPfSaJourneySuccessCredit)
        val expectedSummaryListRows = Seq(
          SummaryListRow(Key(Text("Swm a dalwyd i CThEM"), ""), Value(Text("£12.34"), ""), "", None),
          SummaryListRow(Key(HtmlContent("<nobr>Ffi cerdyn (9.97%),</nobr><br/><nobr>ni ellir ei ad-dalu</nobr>"), ""), Value(Text("£1.23"), ""), "", None),
          SummaryListRow(Key(Text("Cyfanswm a dalwyd"), ""), Value(Text("£13.57"), ""), "", None)
        )
        summaryListRows shouldBe expectedSummaryListRows
      }

      "should return the relevant amounts for credit card payments in GDS format (i.e. don't show £x.00 when pennies is 0)" in {
        implicit val messages: Messages = messagesApi.preferred(fakeGetRequest)
        val orderWithPoundsZeroPennies = Some(BarclaysOrder(
          transactionReference = TransactionReference("Some-transaction-ref"),
          iFrameUrl            = Url("some-url"),
          cardCategory         = Some(CardCategories.credit),
          commissionInPence    = Some(AmountInPence(12300)),
          paidOn               = Some(LocalDateTime.parse("2027-11-02T16:28:55.185"))
        ))
        val journey = TestJourneys.PfSa.testPfSaJourneySuccessCredit.copy(order         = orderWithPoundsZeroPennies, amountInPence = Some(AmountInPence(1234000)))
        val summaryListRows: Seq[SummaryListRow] = PaymentCompleteController.buildAmountsSummaryListRow(journey)
        val expectedSummaryListRows = Seq(
          SummaryListRow(Key(Text("Amount paid to HMRC"), ""), Value(Text("£12,340"), ""), "", None),
          SummaryListRow(Key(HtmlContent("<nobr>Card fee (1.00%),</nobr><br/><nobr>non-refundable</nobr>"), ""), Value(Text("£123"), ""), "", None),
          SummaryListRow(Key(Text("Total paid"), ""), Value(Text("£12,463"), ""), "", None)
        )
        summaryListRows shouldBe expectedSummaryListRows
      }
    }
  }

}
