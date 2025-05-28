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
import org.scalatest.Assertion
import payapi.cardpaymentjourney.model.journey.{Journey, JourneySpecificData}
import payapi.corcommon.model.{JourneyId, Origin, Origins}
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, status}
import uk.gov.hmrc.cardpaymentfrontend.testsupport.TestOps.FakeRequestOps
import uk.gov.hmrc.cardpaymentfrontend.testsupport.stubs.PayApiStub
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.TestJourneys
import uk.gov.hmrc.cardpaymentfrontend.testsupport.{ItSpec, TestHelpers}

import scala.jdk.CollectionConverters.ListHasAsScala

class CheckYourAnswersControllerSpec extends ItSpec {

  val systemUnderTest: CheckYourAnswersController = app.injector.instanceOf[CheckYourAnswersController]

  def fakeGetRequest(journeyId: JourneyId = TestJourneys.PfSa.journeyBeforeBeginWebPayment._id): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest()
      .withSessionId()
      .withEmailAndAddressInSession(journeyId)
  def fakeGetRequestInWelsh(journeyId: JourneyId = TestJourneys.PfSa.journeyBeforeBeginWebPayment._id): FakeRequest[AnyContentAsEmpty.type] = fakeGetRequest(journeyId).withLangWelsh()

  "GET /check-your-details" - {

    "should return 200 OK" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
      val result = systemUnderTest.renderPage(fakeGetRequest())
      status(result) shouldBe Status.OK
    }

    "should render the page with the language toggle" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
      val result = systemUnderTest.renderPage(fakeGetRequest())
      val document = Jsoup.parse(contentAsString(result))
      val langToggleText: List[String] = document.select(".hmrc-language-select__list-item").eachText().asScala.toList
      langToggleText should contain theSameElementsAs List("English", "Newid yr iaith ir Gymraeg Cymraeg") //checking the visually hidden text, it's simpler
    }

    "show the Title tab correctly in English" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
      val result = systemUnderTest.renderPage(fakeGetRequest())
      val document = Jsoup.parse(contentAsString(result))
      document.title shouldBe "Check your details - Pay your Self Assessment - GOV.UK"
    }

    "show the Title tab correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
      val result = systemUnderTest.renderPage(fakeGetRequestInWelsh())
      val document = Jsoup.parse(contentAsString(result))
      document.title shouldBe "Gwiriwch eich manylion - Talu eich Hunanasesiad - GOV.UK"
    }

    "show the Service Name banner title correctly in English" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
      val result = systemUnderTest.renderPage(fakeGetRequest())
      val document = Jsoup.parse(contentAsString(result))
      document.select(".govuk-header__service-name").html shouldBe "Pay your Self Assessment"
    }

    "show the Service Name banner title correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
      val result = systemUnderTest.renderPage(fakeGetRequestInWelsh())
      val document = Jsoup.parse(contentAsString(result))
      document.select(".govuk-header__service-name").html shouldBe "Talu eich Hunanasesiad"
    }

    "should render the h1 correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
      val result = systemUnderTest.renderPage(fakeGetRequest())
      val document = Jsoup.parse(contentAsString(result))
      document.select("h1").text() shouldBe "Check your details"
    }

    "should render the h1 correctly in welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
      val result = systemUnderTest.renderPage(fakeGetRequestInWelsh())
      val document = Jsoup.parse(contentAsString(result))
      document.select("h1").text() shouldBe "Gwiriwch eich manylion"
    }

    "should render the continue button" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
      val result = systemUnderTest.renderPage(fakeGetRequest())
      val document = Jsoup.parse(contentAsString(result))
      document.select("#submit").text() shouldBe "Continue"
    }

    "should render the continue button in welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
      val result = systemUnderTest.renderPage(fakeGetRequestInWelsh())
      val document = Jsoup.parse(contentAsString(result))
      document.select("#submit").text() shouldBe "Yn eich blaen"
    }

      // derives correct row in summary list due to Origins that may include FDP.
      // The rows show as (with index value):
      // Payment date (optional) 0 or 1
      // Payment reference 1 or 2
      // Email address (optional) 2 or 3
      // Card billing address 3 or 4 (or 2 if there is no email address)
      def deriveReferenceRowIndex(origin: Origin): Int = {
        origin match {
          case Origins.BtaSa => 1
          case Origins.PtaSa => 1
          case Origins.ItSa  => 1
          case _             => 0
        }
      }

      def deriveAmountRowIndex(origin: Origin): Int = {
        origin match {
          case Origins.BtaSa       => 2
          case Origins.PtaSa       => 2
          case Origins.ItSa        => 2
          case Origins.AlcoholDuty => 2
          case _                   => 1
        }
      }

      def deriveEmailRowIndex(origin: Origin): Int = {
        origin match {
          case Origins.BtaSa       => 3
          case Origins.PtaSa       => 3
          case Origins.ItSa        => 3
          case Origins.AlcoholDuty => 3
          case _                   => 2
        }
      }

      def deriveCardBillingAddressRowIndex(origin: Origin): Int = {
        origin match {
          case Origins.BtaSa       => 4
          case Origins.PtaSa       => 4
          case Origins.ItSa        => 4
          case Origins.AlcoholDuty => 4
          case _                   => 3
        }
      }

    TestHelpers.implementedOrigins.foreach {
      origin: Origin =>

        val tdJourney: Journey[JourneySpecificData] = TestHelpers.deriveTestDataFromOrigin(origin).journeyBeforeBeginWebPayment

        s"[${origin.entryName}] should render the amount row correctly" in {
          PayApiStub.stubForFindBySessionId2xx(tdJourney)
          val result = systemUnderTest.renderPage(fakeGetRequest(tdJourney._id))
          val document = Jsoup.parse(contentAsString(result))
          val amountRowIndex = deriveAmountRowIndex(origin)
          val amountRow = document.select(".govuk-summary-list__row").asScala.toList(amountRowIndex)
          assertRow(amountRow, "Total to pay", "£12.34", Some("Change"), Some("http://localhost:9056/pay/change-amount?showSummary=false&stayOnPayFrontend=false"))
        }

        s"[${origin.entryName}] should render the amount row correctly in Welsh" in {
          PayApiStub.stubForFindBySessionId2xx(tdJourney)
          val result = systemUnderTest.renderPage(fakeGetRequestInWelsh(tdJourney._id))
          val document = Jsoup.parse(contentAsString(result))
          val amountRowIndex = deriveAmountRowIndex(origin)
          val amountRow = document.select(".govuk-summary-list__row").asScala.toList(amountRowIndex)
          assertRow(amountRow, "Cyfanswm i’w dalu", "£12.34", Some("Newid"), Some("http://localhost:9056/pay/change-amount?showSummary=false&stayOnPayFrontend=false"))
        }

        //hint, this is so test without email address row does not become obsolete if we changed the value. Stops anyone "forgetting" to update the test.
        val emailAddressKeyText: String = "Email address"
        val emailAddressKeyTextWelsh: String = "Cyfeiriad e-bost"

        s"[${origin.entryName}] render the email address row correctly when there is an email in session" in {
          PayApiStub.stubForFindBySessionId2xx(tdJourney)
          val result = systemUnderTest.renderPage(fakeGetRequest())
          val document = Jsoup.parse(contentAsString(result))
          val emailRow: Element = document.select(".govuk-summary-list__row").asScala.toList(deriveEmailRowIndex(origin))
          assertRow(emailRow, emailAddressKeyText, "blah@blah.com", Some("Change"), Some("/pay-by-card/email-address"))
        }

        s"[${origin.entryName}] render the email address row correctly when there is an email in session in Welsh" in {
          PayApiStub.stubForFindBySessionId2xx(tdJourney)
          val result = systemUnderTest.renderPage(fakeGetRequestInWelsh())
          val document = Jsoup.parse(contentAsString(result))
          val emailRow: Element = document.select(".govuk-summary-list__row").asScala.toList(deriveEmailRowIndex(origin))
          assertRow(emailRow, emailAddressKeyTextWelsh, "blah@blah.com", Some("Newid"), Some("/pay-by-card/email-address"))
        }

        s"[${origin.entryName}] not render the email address row when there is not an email in session" in {
          PayApiStub.stubForFindBySessionId2xx(tdJourney)
          val result = systemUnderTest.renderPage(FakeRequest().withSessionId().withAddressInSession(tdJourney._id))
          contentAsString(result) shouldNot include(emailAddressKeyText)
        }

        s"[${origin.entryName}] render the card billing address row correctly" in {
          PayApiStub.stubForFindBySessionId2xx(tdJourney)
          val result = systemUnderTest.renderPage(fakeGetRequest())
          val document = Jsoup.parse(contentAsString(result))
          val cardBillingAddressRow: Element = document.select(".govuk-summary-list__row").asScala.toList(deriveCardBillingAddressRowIndex(origin))
          assertRow(cardBillingAddressRow, "Card billing address", "line1 AA0AA0", Some("Change"), Some("/pay-by-card/address"))
        }

        s"[${origin.entryName}] render the card billing address row correctly in Welsh" in {
          PayApiStub.stubForFindBySessionId2xx(tdJourney)
          val result = systemUnderTest.renderPage(fakeGetRequestInWelsh())
          val document = Jsoup.parse(contentAsString(result))
          val cardBillingAddressRow: Element = document.select(".govuk-summary-list__row").asScala.toList(deriveCardBillingAddressRowIndex(origin))
          assertRow(cardBillingAddressRow, "Cyfeiriad bilio", "line1 AA0AA0", Some("Newid"), Some("/pay-by-card/address"))
        }

        s"[${origin.entryName}] throw an exception when there is no card billing address in the session" in {
          PayApiStub.stubForFindBySessionId2xx(tdJourney)
          val exception: RuntimeException = intercept[RuntimeException] {
            systemUnderTest.renderPage(FakeRequest().withSessionId()).futureValue
          }
          exception.getMessage should include("Cannot take a card payment without an address.")
        }
    }

    "[PfSa] should render the payment reference row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
      val result = systemUnderTest.renderPage(fakeGetRequest())
      val document = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList(deriveReferenceRowIndex(Origins.PfSa))
      assertRow(referenceRow, "Unique Taxpayer Reference (UTR)", "1234567895K", Some("Change"), Some("http://localhost:9056/pay/pay-by-card-change-reference-number"))
    }

    "[PfSa] should render the payment reference row correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
      val result = systemUnderTest.renderPage(fakeGetRequestInWelsh())
      val document = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList(deriveReferenceRowIndex(Origins.PfSa))
      assertRow(referenceRow, "Cyfeirnod Unigryw y Trethdalwr (UTR)", "1234567895K", Some("Newid"), Some("http://localhost:9056/pay/pay-by-card-change-reference-number"))
    }

    "[BtaSa] should render the payment reference row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaSa.journeyBeforeBeginWebPayment)
      val result = systemUnderTest.renderPage(fakeGetRequest())
      val document = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList(deriveReferenceRowIndex(Origins.BtaSa))
      assertRow(referenceRow, "Unique Taxpayer Reference (UTR)", "1234567895K", None, None)
    }

    "[BtaSa] should render the payment reference row correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaSa.journeyBeforeBeginWebPayment)
      val result = systemUnderTest.renderPage(fakeGetRequestInWelsh())
      val document = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList(deriveReferenceRowIndex(Origins.BtaSa))
      assertRow(referenceRow, "Cyfeirnod Unigryw y Trethdalwr (UTR)", "1234567895K", None, None)
    }

    "[PtaSa] should render the payment reference row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PtaSa.journeyBeforeBeginWebPayment)
      val result = systemUnderTest.renderPage(fakeGetRequest())
      val document = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList(deriveReferenceRowIndex(Origins.PtaSa))
      assertRow(referenceRow, "Unique Taxpayer Reference (UTR)", "1234567895K", None, None)
    }

    "[PtaSa] should render the payment reference row correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PtaSa.journeyBeforeBeginWebPayment)
      val result = systemUnderTest.renderPage(fakeGetRequestInWelsh())
      val document = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList(deriveReferenceRowIndex(Origins.PtaSa))
      assertRow(referenceRow, "Cyfeirnod Unigryw y Trethdalwr (UTR)", "1234567895K", None, None)
    }

    "[PfAlcoholDuty] should render the payment reference row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfAlcoholDuty.journeyBeforeBeginWebPayment)
      val result = systemUnderTest.renderPage(fakeGetRequest())
      val document = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList(deriveReferenceRowIndex(Origins.PfAlcoholDuty))
      assertRow(referenceRow, "Payment reference", "XMADP0123456789", Some("Change"), Some("http://localhost:9056/pay/pay-by-card-change-reference-number"))
    }

    "[PfAlcoholDuty] should render the payment reference row correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfAlcoholDuty.journeyBeforeBeginWebPayment)
      val result = systemUnderTest.renderPage(fakeGetRequestInWelsh())
      val document = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList(deriveReferenceRowIndex(Origins.PfAlcoholDuty))
      assertRow(referenceRow, "Cyfeirnod y taliad", "XMADP0123456789", Some("Newid"), Some("http://localhost:9056/pay/pay-by-card-change-reference-number"))
    }

    "[AlcoholDuty] should render the payment reference row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.AlcoholDuty.journeyBeforeBeginWebPayment)
      val result = systemUnderTest.renderPage(fakeGetRequest())
      val document = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList(deriveReferenceRowIndex(Origins.AlcoholDuty))
      assertRow(referenceRow, "Payment reference", "XMADP0123456789", None, None)
    }

    "[AlcoholDuty] should render the payment reference row correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.AlcoholDuty.journeyBeforeBeginWebPayment)
      val result = systemUnderTest.renderPage(fakeGetRequestInWelsh())
      val document = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList(deriveReferenceRowIndex(Origins.AlcoholDuty))
      assertRow(referenceRow, "Cyfeirnod y taliad", "XMADP0123456789", None, None)
    }

    "[AlcoholDuty] should render the charge reference row correctly when it's available" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.AlcoholDuty.journeyBeforeBeginWebPayment)
      val result = systemUnderTest.renderPage(fakeGetRequest())
      val document = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList(1)
      assertRow(referenceRow, "Charge reference", "XE1234567890123", None, None)
    }

    "[AlcoholDuty] should render the charge reference row correctly in welsh when it's available" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.AlcoholDuty.journeyBeforeBeginWebPayment)
      val result = systemUnderTest.renderPage(fakeGetRequestInWelsh())
      val document = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList(1)
      assertRow(referenceRow, "Cyfeirnod y tâl", "XE1234567890123", None, None)
    }

    "[ItSa] should render the payment reference row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.ItSa.journeyBeforeBeginWebPayment)
      val result = systemUnderTest.renderPage(fakeGetRequest())
      val document = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList(deriveReferenceRowIndex(Origins.ItSa))
      assertRow(referenceRow, "Unique Taxpayer Reference (UTR)", "1234567895K", None, None)
    }

    "[ItSa] should render the payment reference row correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.ItSa.journeyBeforeBeginWebPayment)
      val result = systemUnderTest.renderPage(fakeGetRequestInWelsh())
      val document = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList(deriveReferenceRowIndex(Origins.ItSa))
      assertRow(referenceRow, "Cyfeirnod Unigryw y Trethdalwr (UTR)", "1234567895K", None, None)
    }

    "[BtaSa] should render the payment date row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaSa.journeyBeforeBeginWebPayment)
      val result = systemUnderTest.renderPage(fakeGetRequest())
      val document = Jsoup.parse(contentAsString(result))
      val paymentDateRow = document.select(".govuk-summary-list__row").asScala.toList(0)
      assertRow(paymentDateRow, "Payment date", "Today", Some("Change"), Some("http://localhost:9056/pay/change-when-do-you-want-to-pay?toPayFrontendConfirmation=true"))
    }

    "[BtaSa] should render the payment date row correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaSa.journeyBeforeBeginWebPayment)
      val result = systemUnderTest.renderPage(fakeGetRequestInWelsh())
      val document = Jsoup.parse(contentAsString(result))
      val paymentDateRow = document.select(".govuk-summary-list__row").asScala.toList(0)
      assertRow(paymentDateRow, "Dyddiad talu", "Heddiw", Some("Newid"), Some("http://localhost:9056/pay/change-when-do-you-want-to-pay?toPayFrontendConfirmation=true"))
    }

    "[PtaSa] should render the payment date row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PtaSa.journeyBeforeBeginWebPayment)
      val result = systemUnderTest.renderPage(fakeGetRequest())
      val document = Jsoup.parse(contentAsString(result))
      val paymentDateRow = document.select(".govuk-summary-list__row").asScala.toList(0)
      assertRow(paymentDateRow, "Payment date", "Today", Some("Change"), Some("http://localhost:9056/pay/change-when-do-you-want-to-pay?toPayFrontendConfirmation=true"))
    }

    "[PtaSa] should render the payment date row correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PtaSa.journeyBeforeBeginWebPayment)
      val result = systemUnderTest.renderPage(fakeGetRequestInWelsh())
      val document = Jsoup.parse(contentAsString(result))
      val paymentDateRow = document.select(".govuk-summary-list__row").asScala.toList(0)
      assertRow(paymentDateRow, "Dyddiad talu", "Heddiw", Some("Newid"), Some("http://localhost:9056/pay/change-when-do-you-want-to-pay?toPayFrontendConfirmation=true"))
    }

    "sanity check for implemented origins" in {
      TestHelpers.implementedOrigins.size shouldBe 6 withClue "** This dummy test is here to remind you to update the tests above. Bump up the expected number when an origin is added to implemented origins **"
    }

  }

  private def assertRow(element: Element, keyText: String, valueText: String, actionText: Option[String], actionHref: Option[String]): Assertion = {
    element.select(".govuk-summary-list__key").text() shouldBe keyText
    element.select(".govuk-summary-list__value").text() shouldBe valueText

    actionText.fold {
      element.toString should not contain "Change"
      element.select(".govuk-summary-list__actions").asScala.size shouldBe 0
    }(content => element.select(".govuk-summary-list__actions").text() shouldBe content)

    actionHref.fold(element.select(".govuk-summary-list__actions").select("a").text() shouldBe "")(href => element.select(".govuk-summary-list__actions").select("a").attr("href") shouldBe href)
  }

}
