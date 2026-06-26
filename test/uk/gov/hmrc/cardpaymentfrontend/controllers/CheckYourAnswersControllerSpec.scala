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
import org.jsoup.nodes.Element
import org.scalatest.Assertion
import payapi.cardpaymentjourney.model.barclays.BarclaysOrder
import payapi.cardpaymentjourney.model.journey.{Journey, JourneySpecificData, Url}
import payapi.corcommon.model.barclays.TransactionReference
import payapi.corcommon.model.{JourneyId, Origin, Origins}
import play.api.http.Status
import play.api.http.Status.SEE_OTHER
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, redirectLocation, status}
import uk.gov.hmrc.cardpaymentfrontend.models.cardpayment.CardPaymentInitiatePaymentResponse
import uk.gov.hmrc.cardpaymentfrontend.services.CryptoService
import uk.gov.hmrc.cardpaymentfrontend.testsupport.TestOps.FakeRequestOps
import uk.gov.hmrc.cardpaymentfrontend.testsupport.stubs.{CardPaymentStub, PayApiStub}
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.TestJourneys
import uk.gov.hmrc.cardpaymentfrontend.testsupport.{ItSpec, TestHelpers}

import scala.jdk.CollectionConverters.ListHasAsScala

class CheckYourAnswersControllerSpec extends ItSpec {

  val systemUnderTest: CheckYourAnswersController = app.injector.instanceOf[CheckYourAnswersController]
  val cryptoService: CryptoService                = app.injector.instanceOf[CryptoService]

  def fakeRequest(journeyId: JourneyId = TestJourneys.PfSa.journeyBeforeBeginWebPayment._id): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest()
      .withSessionId()
      .withEmailAndAddressInSession(cryptoService, journeyId)

  def fakeRequestWelsh(journeyId: JourneyId = TestJourneys.PfSa.journeyBeforeBeginWebPayment._id): FakeRequest[AnyContentAsEmpty.type] =
    fakeRequest(journeyId).withLangWelsh()

  "GET /check-your-details" - {

    "should return 200 OK" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
      val result = systemUnderTest.renderPage(fakeRequest())
      status(result) shouldBe Status.OK
    }

    "should render the page with the language toggle" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
      val result                       = systemUnderTest.renderPage(fakeRequest())
      val document                     = Jsoup.parse(contentAsString(result))
      val langToggleText: List[String] = document.select(".hmrc-service-navigation-language-select__list-item").eachText().asScala.toList
      langToggleText should contain theSameElementsAs List("ENG", "CYM – Newid yr iaith i’r Gymraeg") // checking the visually hidden text, it's simpler
    }

    "show the Title tab correctly in English" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
      val result   = systemUnderTest.renderPage(fakeRequest())
      val document = Jsoup.parse(contentAsString(result))
      document.title shouldBe "Check your details - Pay your Self Assessment - GOV.UK"
    }

    "show the Title tab correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
      val result   = systemUnderTest.renderPage(fakeRequestWelsh())
      val document = Jsoup.parse(contentAsString(result))
      document.title shouldBe "Gwiriwch eich manylion - Talu eich Hunanasesiad - GOV.UK"
    }

    "show the Service Name banner title correctly in English" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
      val result   = systemUnderTest.renderPage(fakeRequest())
      val document = Jsoup.parse(contentAsString(result))
      document.select(".govuk-service-navigation__service-name").text() shouldBe "Pay your Self Assessment"
    }

    "show the Service Name banner title correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
      val result   = systemUnderTest.renderPage(fakeRequestWelsh())
      val document = Jsoup.parse(contentAsString(result))
      document.select(".govuk-service-navigation__service-name").text() shouldBe "Talu eich Hunanasesiad"
    }

    "should render the h1 correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
      val result   = systemUnderTest.renderPage(fakeRequest())
      val document = Jsoup.parse(contentAsString(result))
      document.select("h1").text() shouldBe "Check your details"
    }

    "should render the h1 correctly in welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
      val result   = systemUnderTest.renderPage(fakeRequestWelsh())
      val document = Jsoup.parse(contentAsString(result))
      document.select("h1").text() shouldBe "Gwiriwch eich manylion"
    }

    "should render the continue button" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
      val result   = systemUnderTest.renderPage(fakeRequest())
      val document = Jsoup.parse(contentAsString(result))
      document.select("#submit").text() shouldBe "Continue"
    }

    "should render the continue button in welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
      val result   = systemUnderTest.renderPage(fakeRequestWelsh())
      val document = Jsoup.parse(contentAsString(result))
      document.select("#submit").text() shouldBe "Yn eich blaen"
    }

    // derives correct row in summary list due to Origins that may include FDP.
    // The rows show as (with index value):
    // Payment date (optional) 0 or 1
    // Payment reference 0 or not present at all
    // Email address (optional) 1 or 2
    // Card billing address 2 or 3 (or 1 if there is no email address)

    def deriveAmountRowIndex(origin: Origin): Int = {
      origin match {
        case Origins.Amls => 0
        case Origins.AppSimpleAssessment => 2
        case Origins.BtaClass1aNi => 2
        case Origins.BtaCt => 1
        case Origins.CapitalGainsTax => 0
        case Origins.DdSdil => 0
        case Origins.DdVat => 0
        case Origins.JrsJobRetentionScheme => 0
        case Origins.NiEuVatIoss => 3
        case Origins.NiEuVatOss => 3
        case Origins.PfCt => 2
        case Origins.PfEpayeNi => 2
        case Origins.PfEpayeP11d => 2
        case Origins.PfNiEuVatIoss => 3
        case Origins.PfNiEuVatOss => 3
        case Origins.PtaSimpleAssessment => 3
        case Origins.StampTaxesOnShares => 0
        case Origins.VatC2c => 0
        case Origins.VcVatOther => 2
        case Origins.WcChildBenefitRepayments => 0
        case Origins.WcClass1aNi => 0
        case Origins.WcCt => 0
        case Origins.WcEpayeLateCis => 0
        case Origins.WcEpayeLpp => 0
        case Origins.WcEpayeNi => 0
        case Origins.WcEpayeSeta => 0
        case Origins.WcSa => 0
        case Origins.WcSdlt => 0
        case Origins.WcSimpleAssessment => 0
        case Origins.WcXref => 0
        case _ => 1
      }
    }

    def deriveEmailRowIndex(origin: Origin): Int = {
      origin match {
        case Origins.Amls => 1
        case Origins.AppSimpleAssessment => 3
        case Origins.BtaClass1aNi => 3
        case Origins.BtaCt => 2
        case Origins.CapitalGainsTax => 1
        case Origins.DdSdil => 1
        case Origins.DdVat => 1
        case Origins.JrsJobRetentionScheme => 1
        case Origins.NiEuVatIoss => 4
        case Origins.NiEuVatOss => 4
        case Origins.PfCt => 3
        case Origins.PfEpayeNi => 3
        case Origins.PfEpayeP11d => 3
        case Origins.PfNiEuVatIoss => 4
        case Origins.PfNiEuVatOss => 4
        case Origins.PtaSimpleAssessment => 4
        case Origins.StampTaxesOnShares => 1
        case Origins.VatC2c => 1
        case Origins.VcVatOther => 3
        case Origins.WcChildBenefitRepayments => 1
        case Origins.WcClass1aNi => 1
        case Origins.WcCt => 1
        case Origins.WcEpayeLateCis => 1
        case Origins.WcEpayeLpp => 1
        case Origins.WcEpayeNi => 1
        case Origins.WcEpayeSeta => 1
        case Origins.WcSa => 1
        case Origins.WcSdlt => 1
        case Origins.WcSimpleAssessment => 1
        case Origins.WcXref => 1
        case _ => 2
      }
    }

    def deriveCardBillingAddressRowIndex(origin: Origin): Int = {
      origin match {
        case Origins.Amls                => 2
        case Origins.AppSimpleAssessment => 4
        case Origins.BtaClass1aNi        => 4
        case Origins.CapitalGainsTax     => 2
        case Origins.DdSdil              => 2
        case Origins.DdVat               => 2
        case Origins.JrsJobRetentionScheme => 2
        case Origins.NiEuVatIoss         => 5
        case Origins.NiEuVatOss          => 5
        case Origins.PfCt                => 4
        case Origins.PfEpayeNi           => 4
        case Origins.PfEpayeP11d         => 4
        case Origins.PfNiEuVatIoss       => 5
        case Origins.PfNiEuVatOss        => 5
        case Origins.PtaSimpleAssessment => 5
        case Origins.StampTaxesOnShares  => 2
        case Origins.VatC2c              => 2
        case Origins.VcVatOther          => 4
        case Origins.WcChildBenefitRepayments => 2
        case Origins.WcClass1aNi         => 2
        case Origins.WcCt                => 2
        case Origins.WcEpayeLateCis      => 2
        case Origins.WcEpayeLpp          => 2
        case Origins.WcEpayeNi           => 2
        case Origins.WcEpayeSeta         => 2
        case Origins.WcSa                => 2
        case Origins.WcSdlt              => 2
        case Origins.WcSimpleAssessment  => 2
        case Origins.WcXref              => 2
        case _                           => 3
      }
    }

    TestHelpers.implementedOrigins.foreach { (origin: Origin) =>

      val tdJourney: Journey[JourneySpecificData] = TestHelpers.deriveTestDataFromOrigin(origin).journeyBeforeBeginWebPayment

      val shouldBeAbleToChangeAmount: Boolean = origin match {
        case Origins.WcSa               => false
        case Origins.WcCt               => false
        case Origins.WcVat              => false
        case Origins.WcSimpleAssessment => false
        case Origins.WcXref             => false
        case Origins.VatC2c             => false
        case Origins.WcEpayeLpp         => false
        case Origins.WcClass1aNi        => false
        case Origins.WcEpayeNi          => false
        case Origins.WcEpayeLateCis     => false
        case Origins.WcEpayeSeta        => false
        case Origins.Mib                => false
        case Origins.BcPngr             => false
        case Origins.WcSdlt             => false
        case Origins.StampTaxesOnShares => false
        case _                          => true
      }

      s"[${origin.entryName}] should render the amount row correctly" in {
        PayApiStub.stubForFindBySessionId2xx(tdJourney)
        val result         = systemUnderTest.renderPage(fakeRequest(tdJourney._id))
        val document       = Jsoup.parse(contentAsString(result))
        val amountRowIndex = deriveAmountRowIndex(origin)
        val amountRow      = document.select(".govuk-summary-list__row").asScala.toList(amountRowIndex)
        assertRow(
          element = amountRow,
          keyText = "Amount",
          valueText = "£12.34",
          actionText = if (shouldBeAbleToChangeAmount) Some("Change Amount") else None,
          actionHref = if (shouldBeAbleToChangeAmount) Some("http://localhost:9056/pay/change-amount?showSummary=true") else None
        )
      }

      s"[${origin.entryName}] should render the amount row correctly in Welsh" in {
        PayApiStub.stubForFindBySessionId2xx(tdJourney)
        val result         = systemUnderTest.renderPage(fakeRequestWelsh(tdJourney._id))
        val document       = Jsoup.parse(contentAsString(result))
        val amountRowIndex = deriveAmountRowIndex(origin)
        val amountRow      = document.select(".govuk-summary-list__row").asScala.toList(amountRowIndex)
        assertRow(
          element = amountRow,
          keyText = "Swm",
          valueText = "£12.34",
          actionText = if (shouldBeAbleToChangeAmount) Some("Newid Swm") else None,
          actionHref = if (shouldBeAbleToChangeAmount) Some("http://localhost:9056/pay/change-amount?showSummary=true") else None
        )
      }

      // hint, this is so test without email address row does not become obsolete if we changed the value. Stops anyone "forgetting" to update the test.
      val emailAddressKeyText: String      = "Email address"
      val emailAddressKeyTextWelsh: String = "Cyfeiriad e-bost"

      s"[${origin.entryName}] render the email address row correctly when there is an email in session" in {
        PayApiStub.stubForFindBySessionId2xx(tdJourney)
        val result            = systemUnderTest.renderPage(fakeRequest())
        val document          = Jsoup.parse(contentAsString(result))
        val emailRow: Element = document.select(".govuk-summary-list__row").asScala.toList(deriveEmailRowIndex(origin))
        assertRow(emailRow, emailAddressKeyText, "blah@blah.com", Some("Change Email address"), Some("/pay-by-card/email-address"))
      }

      s"[${origin.entryName}] render the email address row correctly when there is an email in session in Welsh" in {
        PayApiStub.stubForFindBySessionId2xx(tdJourney)
        val result            = systemUnderTest.renderPage(fakeRequestWelsh())
        val document          = Jsoup.parse(contentAsString(result))
        val emailRow: Element = document.select(".govuk-summary-list__row").asScala.toList(deriveEmailRowIndex(origin))
        assertRow(emailRow, emailAddressKeyTextWelsh, "blah@blah.com", Some("Newid Cyfeiriad e-bost"), Some("/pay-by-card/email-address"))
      }

      s"[${origin.entryName}] not render the email address row when there is not an email in session" in {
        PayApiStub.stubForFindBySessionId2xx(tdJourney)
        val result = systemUnderTest.renderPage(FakeRequest().withSessionId().withAddressInSession(cryptoService, tdJourney._id))
        contentAsString(result) shouldNot include(emailAddressKeyText)
      }

      s"[${origin.entryName}] render the card billing address row correctly" in {
        PayApiStub.stubForFindBySessionId2xx(tdJourney)
        val result                         = systemUnderTest.renderPage(fakeRequest())
        val document                       = Jsoup.parse(contentAsString(result))
        val cardBillingAddressRow: Element = document.select(".govuk-summary-list__row").asScala.toList(deriveCardBillingAddressRowIndex(origin))
        assertRow(cardBillingAddressRow, "Card billing address", "line1 AA0AA0", Some("Change Card billing address"), Some("/pay-by-card/address"))
      }

      s"[${origin.entryName}] render the card billing address row correctly in Welsh" in {
        PayApiStub.stubForFindBySessionId2xx(tdJourney)
        val result                         = systemUnderTest.renderPage(fakeRequestWelsh())
        val document                       = Jsoup.parse(contentAsString(result))
        val cardBillingAddressRow: Element = document.select(".govuk-summary-list__row").asScala.toList(deriveCardBillingAddressRowIndex(origin))
        assertRow(cardBillingAddressRow, "Cyfeiriad bilio", "line1 AA0AA0", Some("Newid Cyfeiriad bilio"), Some("/pay-by-card/address"))
      }

      s"[${origin.entryName}] should redirect to the Address page if no Address in session" in {
        PayApiStub.stubForFindBySessionId2xx(tdJourney)
        val result = systemUnderTest.renderPage(FakeRequest().withSessionId())
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some("/pay-by-card/address")
      }
    }

    "[PfSa] should render the payment reference row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(
        referenceRow,
        "Unique Taxpayer Reference (UTR)",
        "1234567895K",
        Some("Change Unique Taxpayer Reference (UTR)"),
        Some("http://localhost:9056/pay/pay-by-card-change-reference-number")
      )
    }

    "[PfSa] should render the payment reference row correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(
        referenceRow,
        "Cyfeirnod Unigryw y Trethdalwr (UTR)",
        "1234567895K",
        Some("Newid Cyfeirnod Unigryw y Trethdalwr (UTR)"),
        Some("http://localhost:9056/pay/pay-by-card-change-reference-number")
      )
    }

    "[BtaSa] should not render the payment reference in first row" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaSa.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Unique Taxpayer Reference (UTR)")
    }

    "[BtaSa] should not render the payment reference in first row in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaSa.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Cyfeirnod Unigryw y Trethdalwr (UTR)")
    }

    "[PtaSa] should not render the payment reference in first row" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PtaSa.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Unique Taxpayer Reference (UTR)")
    }

    "[PtaSa] should not render the payment reference in first row in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PtaSa.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Cyfeirnod Unigryw y Trethdalwr (UTR)")
    }

    "[PfAlcoholDuty] should render the payment reference row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfAlcoholDuty.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(
        referenceRow,
        "Payment reference",
        "XMADP0123456789",
        Some("Change Payment reference"),
        Some("http://localhost:9056/pay/pay-by-card-change-reference-number")
      )
    }

    "[PfAlcoholDuty] should render the payment reference row correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfAlcoholDuty.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(
        referenceRow,
        "Cyfeirnod y taliad",
        "XMADP0123456789",
        Some("Newid Cyfeirnod y taliad"),
        Some("http://localhost:9056/pay/pay-by-card-change-reference-number")
      )
    }

    "[AlcoholDuty] should not render the payment reference in first row" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.AlcoholDuty.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Payment reference")
    }

    "[AlcoholDuty] should not render the payment reference in first row in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.AlcoholDuty.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Cyfeirnod y taliad")
    }

    "[AlcoholDuty] should render the charge reference row correctly when it's available" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.AlcoholDuty.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(referenceRow, "Charge reference", "XE1234567890123", None, None)
    }

    "[AlcoholDuty] should render the charge reference row correctly in welsh when it's available" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.AlcoholDuty.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(referenceRow, "Cyfeirnod y tâl", "XE1234567890123", None, None)
    }

    "[PfVat] should render the payment reference row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfVat.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(referenceRow, "VAT registration number", "999964805", None, None)
    }

    "[PfVat] should render the payment reference row correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfVat.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(referenceRow, "Rhif cofrestru TAW", "999964805", None, None)
    }

    "[PfVat] should render the charge reference row correctly when it's available" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfVatWithChargeReference.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList(0)
      assertRow(referenceRow, "Charge reference", "XE123456789012", None, None)
    }

    "[PfVat] should render the charge reference row correctly in welsh when it's available" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfVatWithChargeReference.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList(0)
      assertRow(referenceRow, "Cyfeirnod y tâl", "XE123456789012", None, None)
    }

    "[WcVat] should render the payment reference row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.WcVat.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(referenceRow, "VAT registration number", "999964805", None, None)
    }

    "[WcVat] should render the payment reference row correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.WcVat.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(referenceRow, "Rhif cofrestru TAW", "999964805", None, None)
    }

    "[WcVat] should render the charge reference row correctly when it's available" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.WcVatWithChargeReference.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList(0)
      assertRow(referenceRow, "VAT surcharge or penalty reference", "XE123456789012", None, None)
    }

    "[WcVat] should render the charge reference row correctly in welsh when it's available" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.WcVatWithChargeReference.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList(0)
      assertRow(referenceRow, "Gordal TAW neu cyfeirnod y gosb", "XE123456789012", None, None)
    }

    "[BtaVat] should not render the payment reference in first row" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaVat.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "VAT registration number")
    }

    "[BtaVat] should not render the payment reference in first row in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaVat.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Rhif cofrestru TAW")
    }

    "[DdVat] should not render the payment reference in first row" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.DdVat.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "VAT registration number")
    }

    "[DdVat] should not render the payment reference in first row in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.DdVat.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Rhif cofrestru TAW")
    }

    "[VcVatReturn] should not render the payment reference in first row" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.VcVatReturn.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "VAT registration number")
    }

    "[VcVatReturn] should not render the payment reference in first row in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.VcVatReturn.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Rhif cofrestru TAW")
    }

    "[VcVatOther] should should not render the payment reference in first row" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.VcVatOther.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "VAT registration number")
    }

    "[VcVatOther] should not render the payment reference in first row in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.VcVatOther.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Rhif cofrestru TAW")
    }

    "[VcVatOther] should render the charge reference row correctly when it's available" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.VcVatOther.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList(1)
      assertRow(referenceRow, "Charge reference", "999964805", None, None)
    }

    "[VcVatOther] should render the charge reference row correctly in welsh when it's available" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.VcVatOther.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList(1)
      assertRow(referenceRow, "Cyfeirnod y tâl", "999964805", None, None)
    }

    "[ItSa] should not render the payment reference in first row" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.ItSa.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Unique Taxpayer Reference (UTR)")
    }

    "[ItSa] should not render the payment reference in first row in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.ItSa.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Cyfeirnod Unigryw y Trethdalwr (UTR)")
    }

    "[WcSa] should not render the payment reference in first row" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.WcSa.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Unique Taxpayer Reference (UTR)")
    }

    "[WcSa] should not render the payment reference in first row in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.WcSa.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Cyfeirnod Unigryw y Trethdalwr (UTR)")
    }

    "[WcCt] should not render the payment reference in first row" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.WcCt.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Payment reference")
    }

    "[WcCt] should not render the payment reference in first row in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.WcCt.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Cyfeirnod y taliad")
    }

    "[BtaCt] should not render the payment reference in first row" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaCt.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Payment reference")
    }

    "[BtaCt] should not render the payment reference in first row in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaCt.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Cyfeirnod y taliad")
    }

    "[PfCt] should render the payment reference row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfCt.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(referenceRow, "Unique Taxpayer Reference (UTR)", "1097172564", None, None)
    }

    "[PfCt] should render the payment reference row correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfCt.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(referenceRow, "Cyfeirnod Unigryw y Trethdalwr (UTR)", "1097172564", None, None)
    }

    "[PfCt] should render the Payslip reference row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfCt.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList(1)
      assertRow(
        referenceRow,
        "Payslip reference",
        "1097172564A00101A",
        Some("Change Payslip reference"),
        Some("http://localhost:9056/pay/pay-by-card-change-reference-number")
      )
    }

    "[PfCt] should render the Payslip reference row correctly in welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfCt.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList(1)
      assertRow(
        referenceRow,
        "Cyfeirnod slip talu",
        "1097172564A00101A",
        Some("Newid Cyfeirnod slip talu"),
        Some("http://localhost:9056/pay/pay-by-card-change-reference-number")
      )
    }

    "[Ppt] should not render the payment reference in first row" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.Ppt.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Reference number")
    }

    "[Ppt] should not render the payment reference in first row in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.Ppt.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Cyfeirnod")
    }

    "[PfPpt] should render the payment reference row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfPpt.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(
        referenceRow,
        "Reference number",
        "XAPPT0000012345",
        Some("Change Reference number"),
        Some("http://localhost:9056/pay/pay-by-card-change-reference-number")
      )
    }

    "[PfPpt] should render the payment reference row correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfPpt.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(referenceRow, "Cyfeirnod", "XAPPT0000012345", Some("Newid Cyfeirnod"), Some("http://localhost:9056/pay/pay-by-card-change-reference-number"))
    }

    "[BtaEpayeBill] should not render the payment reference in first row" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaEpayeBill.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Payment reference")
    }

    "[BtaEpayeBill] should not render the payment reference in first row in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaEpayeBill.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Cyfeirnod y taliad")
    }

    "[BtaEpayePenalty] should not render the payment reference in first row" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaEpayePenalty.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Payment reference")
    }

    "[BtaEpayePenalty] should not render the payment reference in first row in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaEpayePenalty.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Cyfeirnod y taliad")
    }

    "[BtaEpayeInterest] should not render the payment reference in first row" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaEpayeInterest.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Payment reference")
    }

    "[BtaEpayeInterest] should not render the payment reference in first row in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaEpayeInterest.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Cyfeirnod y taliad")
    }

    "[BtaEpayeGeneral] should not render the payment reference in first row" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaEpayeGeneral.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Payment reference")
    }

    "[BtaEpayeGeneral] should not render the payment reference in first row in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaEpayeGeneral.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Cyfeirnod y taliad")
    }

    "[BtaClass1aNi] should not render the payment reference in first row" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaClass1aNi.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Payment reference")
    }

    "[BtaClass1aNi] should not render the payment reference in first row in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaClass1aNi.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Cyfeirnod y taliad")
    }

    "[PfAmls] should render the payment reference row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfAmls.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(
        referenceRow,
        "Payment reference",
        "XE123456789012",
        Some("Change Payment reference"),
        Some("http://localhost:9056/pay/pay-by-card-change-reference-number")
      )
    }

    "[PfAmls] should render the payment reference row correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfAmls.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(
        referenceRow,
        "Cyfeirnod y taliad",
        "XE123456789012",
        Some("Newid Cyfeirnod y taliad"),
        Some("http://localhost:9056/pay/pay-by-card-change-reference-number")
      )
    }

    "[Amls] should not render the payment reference in first row" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.Amls.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Payment reference")
    }

    "[Amls] should not render the payment reference in first row in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.Amls.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Cyfeirnod y taliad")
    }

    "[PfEconomicCrimeLevy] should render the payment reference row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfEconomicCrimeLevy.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(
        referenceRow,
        "Reference number",
        "XE123456789012",
        Some("Change Reference number"),
        Some("http://localhost:9056/pay/pay-by-card-change-reference-number")
      )
    }

    "[PfEconomicCrimeLevy] should render the payment reference row correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfEconomicCrimeLevy.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(
        referenceRow,
        "Cyfeirnod y taliad",
        "XE123456789012",
        Some("Newid Cyfeirnod y taliad"),
        Some("http://localhost:9056/pay/pay-by-card-change-reference-number")
      )
    }

    "[EconomicCrimeLevy] should not render the payment reference in first row" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.EconomicCrimeLevy.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Reference number")
    }

    "[EconomicCrimeLevy] should not render the payment reference in first row in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.EconomicCrimeLevy.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Cyfeirnod y taliad")
    }

    "[PfVatC2c] should render the payment reference row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfVatC2c.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(
        referenceRow,
        "Reference number",
        "XVC1A2B3C4D5E6F",
        Some("Change Reference number"),
        Some("http://localhost:9056/pay/pay-by-card-change-reference-number")
      )
    }

    "[PfVatC2c] should render the payment reference row correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfVatC2c.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(
        referenceRow,
        "Cyfeirnod y taliad",
        "XVC1A2B3C4D5E6F",
        Some("Newid Cyfeirnod y taliad"),
        Some("http://localhost:9056/pay/pay-by-card-change-reference-number")
      )
    }

    "[VatC2c] should not render the payment reference in first row" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.VatC2c.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Reference number")
    }

    "[VatC2c] should not render the payment reference in first row in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.VatC2c.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Cyfeirnod y taliad")
    }

    "[BtaSa] should render the payment date row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaSa.journeyBeforeBeginWebPayment)
      val result         = systemUnderTest.renderPage(fakeRequest())
      val document       = Jsoup.parse(contentAsString(result))
      val paymentDateRow = document.select(".govuk-summary-list__row").asScala.toList(0)
      assertRow(
        paymentDateRow,
        "Payment date",
        "Today",
        Some("Change Payment date"),
        Some("http://localhost:9056/pay/change-when-do-you-want-to-pay?toPayFrontendConfirmation=true")
      )
    }

    "[BtaSa] should render the payment date row correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaSa.journeyBeforeBeginWebPayment)
      val result         = systemUnderTest.renderPage(fakeRequestWelsh())
      val document       = Jsoup.parse(contentAsString(result))
      val paymentDateRow = document.select(".govuk-summary-list__row").asScala.toList(0)
      assertRow(
        paymentDateRow,
        "Dyddiad talu",
        "Heddiw",
        Some("Newid Dyddiad talu"),
        Some("http://localhost:9056/pay/change-when-do-you-want-to-pay?toPayFrontendConfirmation=true")
      )
    }

    "[PtaSa] should render the payment date row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PtaSa.journeyBeforeBeginWebPayment)
      val result         = systemUnderTest.renderPage(fakeRequest())
      val document       = Jsoup.parse(contentAsString(result))
      val paymentDateRow = document.select(".govuk-summary-list__row").asScala.toList(0)
      assertRow(
        paymentDateRow,
        "Payment date",
        "Today",
        Some("Change Payment date"),
        Some("http://localhost:9056/pay/change-when-do-you-want-to-pay?toPayFrontendConfirmation=true")
      )
    }

    "[PtaSa] should render the payment date row correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PtaSa.journeyBeforeBeginWebPayment)
      val result         = systemUnderTest.renderPage(fakeRequestWelsh())
      val document       = Jsoup.parse(contentAsString(result))
      val paymentDateRow = document.select(".govuk-summary-list__row").asScala.toList(0)
      assertRow(
        paymentDateRow,
        "Dyddiad talu",
        "Heddiw",
        Some("Newid Dyddiad talu"),
        Some("http://localhost:9056/pay/change-when-do-you-want-to-pay?toPayFrontendConfirmation=true")
      )
    }

    "[BtaCt] should render the payment date row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaCt.journeyBeforeBeginWebPayment)
      val result         = systemUnderTest.renderPage(fakeRequest())
      val document       = Jsoup.parse(contentAsString(result))
      val paymentDateRow = document.select(".govuk-summary-list__row").asScala.toList(0)
      assertRow(
        paymentDateRow,
        "Payment date",
        "Today",
        Some("Change Payment date"),
        Some("http://localhost:9056/pay/change-when-do-you-want-to-pay?toPayFrontendConfirmation=true")
      )
    }

    "[BtaCt] should render the payment date row correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaCt.journeyBeforeBeginWebPayment)
      val result         = systemUnderTest.renderPage(fakeRequestWelsh())
      val document       = Jsoup.parse(contentAsString(result))
      val paymentDateRow = document.select(".govuk-summary-list__row").asScala.toList(0)
      assertRow(
        paymentDateRow,
        "Dyddiad talu",
        "Heddiw",
        Some("Newid Dyddiad talu"),
        Some("http://localhost:9056/pay/change-when-do-you-want-to-pay?toPayFrontendConfirmation=true")
      )
    }

    "[Ppt] should render the payment date row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.Ppt.journeyBeforeBeginWebPayment)
      val result         = systemUnderTest.renderPage(fakeRequest())
      val document       = Jsoup.parse(contentAsString(result))
      val paymentDateRow = document.select(".govuk-summary-list__row").asScala.toList(0)
      assertRow(
        paymentDateRow,
        "Payment date",
        "Today",
        Some("Change Payment date"),
        Some("http://localhost:9056/pay/change-when-do-you-want-to-pay?toPayFrontendConfirmation=true")
      )
    }

    "[Ppt] should render the payment date row correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.Ppt.journeyBeforeBeginWebPayment)
      val result         = systemUnderTest.renderPage(fakeRequestWelsh())
      val document       = Jsoup.parse(contentAsString(result))
      val paymentDateRow = document.select(".govuk-summary-list__row").asScala.toList(0)
      assertRow(
        paymentDateRow,
        "Dyddiad talu",
        "Heddiw",
        Some("Newid Dyddiad talu"),
        Some("http://localhost:9056/pay/change-when-do-you-want-to-pay?toPayFrontendConfirmation=true")
      )
    }

    "[PfEpayeNi] should render the Tax period row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfEpayeNi.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val taxPeriodRow = document.select(".govuk-summary-list__row").asScala.toList(1)
      assertRow(
        taxPeriodRow,
        "Tax period",
        "6 April 2024 to 5 July 2024 (first quarter)",
        Some("Change Tax period"),
        Some("http://localhost:9056/pay/change-employers-paye-period?fromCardPayment=true")
      )
    }

    "[PfEpayeNi] should render the Tax period row correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfEpayeNi.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val taxPeriodRow = document.select(".govuk-summary-list__row").asScala.toList(1)
      assertRow(
        taxPeriodRow,
        "Cyfnod talu",
        "6 Ebrill 2024 i 5 Gorffennaf 2024 (chwarter cyntaf)",
        Some("Newid Cyfnod talu"),
        Some("http://localhost:9056/pay/change-employers-paye-period?fromCardPayment=true")
      )
    }

    "[PfEpayeP11d] should render the Tax year correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfEpayeP11d.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val taxPeriodRow = document.select(".govuk-summary-list__row").asScala.toList(1)
      assertRow(taxPeriodRow, "Tax year", "2024 to 2025", Some("Change Tax year"), Some("http://localhost:9056/pay/change-tax-year?fromCardPayment=true"))
    }

    "[PfEpayeP11d] should render the Tax year correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfEpayeP11d.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val taxPeriodRow = document.select(".govuk-summary-list__row").asScala.toList(1)
      assertRow(
        taxPeriodRow,
        "Blwyddyn dreth",
        "2024 i 2025",
        Some("Newid Blwyddyn dreth"),
        Some("http://localhost:9056/pay/change-tax-year?fromCardPayment=true")
      )
    }

    "[BtaClass1aNi] should render the Tax period correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaClass1aNi.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val taxPeriodRow = document.select(".govuk-summary-list__row").asScala.toList(1)
      assertRow(taxPeriodRow, "Tax period", "2026 to 2027", None, None)
    }

    "[BtaClass1aNi] should render the Tax period correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaClass1aNi.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val taxPeriodRow = document.select(".govuk-summary-list__row").asScala.toList(1)
      assertRow(taxPeriodRow, "Cyfnod talu", "2026 i 2027", None, None)
    }

    "[NiEuVatOss] should render the Tax period correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.NiEuVatOss.journeyBeforeBeginWebPayment)
      val result          = systemUnderTest.renderPage(fakeRequest())
      val document        = Jsoup.parse(contentAsString(result))
      val returnPeriodRow = document.select(".govuk-summary-list__row").asScala.toList(2)
      assertRow(returnPeriodRow, "Return period", "October to December 2024", None, None)
    }

    "[PfNiEuVatOss] should render the Tax period correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfNiEuVatOss.journeyBeforeBeginWebPayment)
      val result          = systemUnderTest.renderPage(fakeRequest())
      val document        = Jsoup.parse(contentAsString(result))
      val returnPeriodRow = document.select(".govuk-summary-list__row").asScala.toList(2)
      assertRow(
        returnPeriodRow,
        "Return period",
        "October to December 2024",
        Some("Change Return period"),
        Some("http://localhost:9056/pay/change-vat-period?fromCardPayment=true")
      )
    }

    "[NiEuVatIoss] should render the Tax period correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.NiEuVatIoss.journeyBeforeBeginWebPayment)
      val result          = systemUnderTest.renderPage(fakeRequest())
      val document        = Jsoup.parse(contentAsString(result))
      val returnPeriodRow = document.select(".govuk-summary-list__row").asScala.toList(2)
      assertRow(returnPeriodRow, "Return period", "June 2024", None, None)
    }

    "[PfNiEuVatIoss] should render the Tax period correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfNiEuVatIoss.journeyBeforeBeginWebPayment)
      val result          = systemUnderTest.renderPage(fakeRequest())
      val document        = Jsoup.parse(contentAsString(result))
      val returnPeriodRow = document.select(".govuk-summary-list__row").asScala.toList(2)
      assertRow(
        returnPeriodRow,
        "Return period",
        "June 2024",
        Some("Change Return period"),
        Some("http://localhost:9056/pay/change-ioss-vat-period?fromCardPayment=true")
      )
    }

    "[BtaVat] should render the payment date row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaVat.journeyBeforeBeginWebPayment)
      val result         = systemUnderTest.renderPage(fakeRequest())
      val document       = Jsoup.parse(contentAsString(result))
      val paymentDateRow = document.select(".govuk-summary-list__row").asScala.toList(0)
      assertRow(
        paymentDateRow,
        "Payment date",
        "Today",
        Some("Change Payment date"),
        Some("http://localhost:9056/pay/change-when-do-you-want-to-pay?toPayFrontendConfirmation=true")
      )
    }

    "[BtaVat] should render the payment date row correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaVat.journeyBeforeBeginWebPayment)
      val result         = systemUnderTest.renderPage(fakeRequestWelsh())
      val document       = Jsoup.parse(contentAsString(result))
      val paymentDateRow = document.select(".govuk-summary-list__row").asScala.toList(0)
      assertRow(
        paymentDateRow,
        "Dyddiad talu",
        "Heddiw",
        Some("Newid Dyddiad talu"),
        Some("http://localhost:9056/pay/change-when-do-you-want-to-pay?toPayFrontendConfirmation=true")
      )
    }

    "[VcVatReturn] should render the payment date row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.VcVatReturn.journeyBeforeBeginWebPayment)
      val result         = systemUnderTest.renderPage(fakeRequest())
      val document       = Jsoup.parse(contentAsString(result))
      val paymentDateRow = document.select(".govuk-summary-list__row").asScala.toList(0)
      assertRow(
        paymentDateRow,
        "Payment date",
        "Today",
        Some("Change Payment date"),
        Some("http://localhost:9056/pay/change-when-do-you-want-to-pay?toPayFrontendConfirmation=true")
      )
    }

    "[VcVatReturn] should render the payment date row correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.VcVatReturn.journeyBeforeBeginWebPayment)
      val result         = systemUnderTest.renderPage(fakeRequestWelsh())
      val document       = Jsoup.parse(contentAsString(result))
      val paymentDateRow = document.select(".govuk-summary-list__row").asScala.toList(0)
      assertRow(
        paymentDateRow,
        "Dyddiad talu",
        "Heddiw",
        Some("Newid Dyddiad talu"),
        Some("http://localhost:9056/pay/change-when-do-you-want-to-pay?toPayFrontendConfirmation=true")
      )
    }

    "[VcVatOther] should render the payment date row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.VcVatOther.journeyBeforeBeginWebPayment)
      val result         = systemUnderTest.renderPage(fakeRequest())
      val document       = Jsoup.parse(contentAsString(result))
      val paymentDateRow = document.select(".govuk-summary-list__row").asScala.toList(0)
      assertRow(
        paymentDateRow,
        "Payment date",
        "Today",
        Some("Change Payment date"),
        Some("http://localhost:9056/pay/change-when-do-you-want-to-pay?toPayFrontendConfirmation=true")
      )
    }

    "[VcVatOther] should render the payment date row correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.VcVatOther.journeyBeforeBeginWebPayment)
      val result         = systemUnderTest.renderPage(fakeRequestWelsh())
      val document       = Jsoup.parse(contentAsString(result))
      val paymentDateRow = document.select(".govuk-summary-list__row").asScala.toList(0)
      assertRow(
        paymentDateRow,
        "Dyddiad talu",
        "Heddiw",
        Some("Newid Dyddiad talu"),
        Some("http://localhost:9056/pay/change-when-do-you-want-to-pay?toPayFrontendConfirmation=true")
      )
    }

    "[BtaClass1aNi] should render the payment date row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaClass1aNi.journeyBeforeBeginWebPayment)
      val result         = systemUnderTest.renderPage(fakeRequest())
      val document       = Jsoup.parse(contentAsString(result))
      val paymentDateRow = document.select(".govuk-summary-list__row").asScala.toList(0)
      assertRow(
        paymentDateRow,
        "Payment date",
        "Today",
        Some("Change Payment date"),
        Some("http://localhost:9056/pay/change-when-do-you-want-to-pay?toPayFrontendConfirmation=true")
      )
    }

    "[BtaClass1aNi] should render the payment date row correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaClass1aNi.journeyBeforeBeginWebPayment)
      val result         = systemUnderTest.renderPage(fakeRequestWelsh())
      val document       = Jsoup.parse(contentAsString(result))
      val paymentDateRow = document.select(".govuk-summary-list__row").asScala.toList(0)
      assertRow(
        paymentDateRow,
        "Dyddiad talu",
        "Heddiw",
        Some("Newid Dyddiad talu"),
        Some("http://localhost:9056/pay/change-when-do-you-want-to-pay?toPayFrontendConfirmation=true")
      )
    }

    "[PfSdlt] should render the payment reference row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSdlt.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(referenceRow, "Unique Transaction Reference Number (UTRN)", "123456789MA", None, None)
    }

    "[PfSdlt] should render the payment reference row correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSdlt.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(referenceRow, "Cyfeirnod Unigryw y Trafodyn (UTRN)", "123456789MA", None, None)
    }

    "[WcSdlt] should not render the payment reference in first row" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.WcSdlt.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Unique Transaction Reference Number (UTRN)")
    }

    "[WcSdlt] should not render the payment reference in first row in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.WcSdlt.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Cyfeirnod Unigryw y Trafodyn (UTRN)")
    }

    "[CapitalGainsTax] should not render the payment reference in first row" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.CapitalGainsTax.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Payment reference")
    }

    "[CapitalGainsTax] should not render the payment reference in first row in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.CapitalGainsTax.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Cyfeirnod y taliad")
    }

    "[WcSimpleAssessment] should not render the payment reference in first row" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.WcSimpleAssessment.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Payment reference")
    }

    "[WcSimpleAssessment] should not render the payment reference in first row in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.WcSimpleAssessment.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Cyfeirnod y taliad")
    }

    "[WcClass1aNi] should not render the payment reference in first row" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.WcClass1aNi.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Payment reference")
    }

    "[WcClass1aNi] should not render the payment reference in first row in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.WcClass1aNi.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Cyfeirnod y taliad")
    }

    "[WcXref] should not render the payment reference in first row" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.WcXref.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Payment reference")
    }

    "[WcXref] should not render the payment reference in first row in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.WcXref.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Cyfeirnod y taliad")
    }

    "[WcEpayeLpp] should not render the payment reference in first row" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.WcEpayeLpp.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Payment reference")
    }

    "[WcEpayeLpp] should not render the payment reference in first row in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.WcEpayeLpp.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Cyfeirnod y taliad")
    }

    "[WcEpayeSeta] should not render the payment reference in first row" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.WcEpayeSeta.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Payment reference")
    }

    "[WcEpayeSeta] should not render the payment reference in first row in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.WcEpayeSeta.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Cyfeirnod y taliad")
    }

    "[WcEpayeNi] should not render the payment reference in first row" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.WcEpayeNi.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Payment reference")
    }

    "[WcEpayeNi] should not render the payment reference in first row in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.WcEpayeNi.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Cyfeirnod y taliad")
    }

    "[WcEpayeLateCis] should not render the payment reference in first row" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.WcEpayeLateCis.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Penalty reference number")
    }

    "[WcEpayeLateCis] should not render the payment reference in first row in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.WcEpayeLateCis.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Cyfeirnod y gosb")
    }

    "[PfChildBenefitRepayments] should render the payment reference row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfChildBenefitRepayments.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(
        referenceRow,
        "Child benefit overpayment reference",
        "YA123456789123",
        Some("Change Child benefit overpayment reference"),
        Some("http://localhost:9056/pay/pay-by-card-change-reference-number")
      )
    }

    "[PfChildBenefitRepayments] should render the payment reference row correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfChildBenefitRepayments.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(
        referenceRow,
        "Cyfeirnod gordaliad Budd-dal Plant",
        "YA123456789123",
        Some("Newid Cyfeirnod gordaliad Budd-dal Plant"),
        Some("http://localhost:9056/pay/pay-by-card-change-reference-number")
      )
    }

    "[WcChildBenefitRepayments] should not render the payment reference in first row" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.WcChildBenefitRepayments.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Child benefit overpayment reference")
    }

    "[WcChildBenefitRepayments] should not render the payment reference in first row in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.WcChildBenefitRepayments.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Cyfeirnod gordaliad Budd-dal Plant")
    }

    "[BtaSdil] should not render the payment reference in first row" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaSdil.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Payment reference")
    }

    "[BtaSdil] should not render the payment reference in first row in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaSdil.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Cyfeirnod y taliad")
    }

    "[DdSdil] should not render the payment reference in first row" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.DdSdil.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Payment reference")
    }

    "[DdSdil] should not render the payment reference in first row in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.DdSdil.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Cyfeirnod y taliad")
    }

    "[PfSdil] should render the payment reference row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSdil.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(
        referenceRow,
        "Payment reference",
        "XE1234567890123",
        Some("Change Payment reference"),
        Some("http://localhost:9056/pay/pay-by-card-change-reference-number")
      )
    }

    "[PfSdil] should render the payment reference row correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSdil.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(
        referenceRow,
        "Cyfeirnod y taliad",
        "XE1234567890123",
        Some("Newid Cyfeirnod y taliad"),
        Some("http://localhost:9056/pay/pay-by-card-change-reference-number")
      )
    }

    "[PfP800] should render the payment reference row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfP800.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(referenceRow, "Reference number", "MA000003AP8002027", None, None)
    }

    "[PfP800] should render the payment reference row correctly in welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfP800.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(referenceRow, "Cyfeirnod", "MA000003AP8002027", None, None)
    }

    "[PfP800] should render the payment reference rows correctly (i.e. show the p800ChargeRef additionally, when there is one)" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfP800.journeyWithP800ChargeRefBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList(1)
      assertRow(referenceRow, "Charge reference", "BC007010065114", None, None)
    }

    "[PfP800] should render the payment reference rows correctly in welsh (i.e. show the p800ChargeRef additionally, when there is one)" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfP800.journeyWithP800ChargeRefBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList(1)
      assertRow(referenceRow, "Cyfeirnod y tâl", "BC007010065114", None, None)
    }

    "[PtaP800] should not render the payment reference in first row" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PtaP800.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Reference number")
    }

    "[PtaP800] should not render the payment reference in first row in welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PtaP800.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Cyfeirnod")
    }

    "[PtaP800] should render the payment reference rows correctly (i.e. show the p800ChargeRef additionally, when there is one)" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PtaP800.journeyWithP800ChargeRefBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(referenceRow, "Charge reference", "BC007010065114", None, None)
    }

    "[PtaP800] should render the payment reference rows correctly in welsh (i.e. show the p800ChargeRef additionally, when there is one)" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PtaP800.journeyWithP800ChargeRefBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(referenceRow, "Cyfeirnod y tâl", "BC007010065114", None, None)
    }

    "[PtaP800] should render the tax year row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PtaP800.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(referenceRow, "Tax year", "6 April 2027 to 5 April 2028", None, None)
    }

    "[PtaP800] should render the tax year row correctly in welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PtaP800.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(referenceRow, "Blwyddyn dreth", "6 Ebrill 2027 i 5 Ebrill 2028", None, None)
    }

    "[PfSimpleAssessment] should render the payment reference row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSimpleAssessment.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(referenceRow, "Reference number", "XE123456789012", None, None)
    }

    "[PfSimpleAssessment] should render the payment reference row correctly in welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSimpleAssessment.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(referenceRow, "Cyfeirnod", "XE123456789012", None, None)
    }

    "[PtaSimpleAssessment] should not render the payment reference in first row" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PtaSimpleAssessment.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Payment reference")
    }

    "[PtaSimpleAssessment] should not render the payment reference in first row in welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PtaSimpleAssessment.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Cyfeirnod y taliad")
    }

    "[PtaSimpleAssessment] should render the charge reference row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PtaSimpleAssessment.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList(1)
      assertRow(referenceRow, "Charge reference", "BC007010065114", None, None)
    }

    "[PtaSimpleAssessment] should render the charge reference row correctly in welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PtaSimpleAssessment.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList(1)
      assertRow(referenceRow, "Cyfeirnod y tâl", "BC007010065114", None, None)
    }

    "[PtaSimpleAssessment] should render the tax year row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PtaSimpleAssessment.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList(2)
      assertRow(referenceRow, "Tax year", "6 April 2027 to 5 April 2028", None, None)
    }

    "[PtaSimpleAssessment] should render the tax year row correctly in welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PtaSimpleAssessment.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList(2)
      assertRow(referenceRow, "Blwyddyn dreth", "6 Ebrill 2027 i 5 Ebrill 2028", None, None)
    }

    "[PfJobRetentionScheme] should render the payment reference row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfJobRetentionScheme.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(
        referenceRow,
        "Payment reference",
        "XJRS12345678901",
        Some("Change Payment reference"),
        Some("http://localhost:9056/pay/pay-by-card-change-reference-number")
      )
    }

    "[PfJobRetentionScheme] should render the payment reference row correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfJobRetentionScheme.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(
        referenceRow,
        "Cyfeirnod y taliad",
        "XJRS12345678901",
        Some("Newid Cyfeirnod y taliad"),
        Some("http://localhost:9056/pay/pay-by-card-change-reference-number")
      )
    }

    "[JrsJobRetentionScheme] should not render the payment reference in first row" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.JrsJobRetentionScheme.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Payment reference")
    }

    "[JrsJobRetentionScheme] should not render the payment reference in first row in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.JrsJobRetentionScheme.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Cyfeirnod y taliad")
    }

    "[PfCds] should render the payment reference row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfCds.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(
        referenceRow,
        "CDS Reference Number",
        "CDSI191234567890",
        Some("Change CDS Reference Number"),
        Some("http://localhost:9056/pay/pay-by-card-change-reference-number")
      )
    }

    "[NiEuVatOss] should not render the payment reference in first row" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.NiEuVatOss.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Payment reference")
    }

    "[PfNiEuVatOss] should render the payment reference row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfNiEuVatOss.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(referenceRow, "Payment reference", "NI101747641Q424", None, None)
    }

    "[NiEuVatIoss] should not render the payment reference in first row" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.NiEuVatIoss.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRowKeyIsNot(referenceRow, "Payment reference")
    }

    "[PfNiEuVatIoss] should render the payment reference row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfNiEuVatIoss.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(referenceRow, "Payment reference", "IM1234567890M0624", None, None)
    }

    "[NiEuVatOss] should render the VAT Number correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.NiEuVatOss.journeyBeforeBeginWebPayment)
      val result          = systemUnderTest.renderPage(fakeRequest())
      val document        = Jsoup.parse(contentAsString(result))
      val returnPeriodRow = document.select(".govuk-summary-list__row").asScala.toList(1)
      assertRow(returnPeriodRow, "VAT Number", "101747641", None, None)
    }

    "[PfNiEuVatOss] should render the VAT Number correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfNiEuVatOss.journeyBeforeBeginWebPayment)
      val result          = systemUnderTest.renderPage(fakeRequest())
      val document        = Jsoup.parse(contentAsString(result))
      val returnPeriodRow = document.select(".govuk-summary-list__row").asScala.toList(1)
      assertRow(returnPeriodRow, "VAT Number", "101747641", Some("Change VAT Number"), Some("http://localhost:9056/pay/pay-by-card-change-reference-number"))
    }

    "[NiEuVatIoss] should render the IOSS Number correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.NiEuVatIoss.journeyBeforeBeginWebPayment)
      val result          = systemUnderTest.renderPage(fakeRequest())
      val document        = Jsoup.parse(contentAsString(result))
      val returnPeriodRow = document.select(".govuk-summary-list__row").asScala.toList(1)
      assertRow(returnPeriodRow, "IOSS Number", "IM1234567890", None, None)
    }

    "[PfNiEuVatIoss] should render the IOSS Number correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfNiEuVatIoss.journeyBeforeBeginWebPayment)
      val result          = systemUnderTest.renderPage(fakeRequest())
      val document        = Jsoup.parse(contentAsString(result))
      val returnPeriodRow = document.select(".govuk-summary-list__row").asScala.toList(1)
      assertRow(
        returnPeriodRow,
        "IOSS Number",
        "IM1234567890",
        Some("Change IOSS Number"),
        Some("http://localhost:9056/pay/pay-by-card-change-reference-number")
      )
    }

    "[AppSa] should render the payment reference row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.AppSa.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(referenceRow, "Unique Taxpayer Reference (UTR)", "1234567890", None, None)
    }

    "[AppSa] should render the payment reference row correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.AppSa.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(referenceRow, "Cyfeirnod Unigryw y Trethdalwr (UTR)", "1234567890", None, None)
    }

    "[AppSimpleAssessment] should render the payment reference row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.AppSimpleAssessment.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(referenceRow, "Payment reference", "MA000003AP3022023", None, None)
    }

    "[AppSimpleAssessment] should render the payment reference row correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.AppSimpleAssessment.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(referenceRow, "Cyfeirnod y taliad", "MA000003AP3022023", None, None)
    }

    "[Mib] should render the payment reference row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.Mib.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(referenceRow, "Declaration reference", "MIBI1234567891", None, None)
    }

    "[Mib] should render the payment reference row correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.Mib.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(referenceRow, "Cyfeirnod y taliad", "MIBI1234567891", None, None)
    }

    "[BcPngr] should render the payment reference row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.BcPngr.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(referenceRow, "Reference number", "XAPR9876543210", None, None)
    }

    "[BcPngr] should render the payment reference row correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.BcPngr.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(referenceRow, "Cyfeirnod y taliad", "XAPR9876543210", None, None)
    }

    "[PfTpes] should render the payment reference row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfTpes.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(
        referenceRow,
        "Reference number",
        "XE123456789012",
        Some("Change Reference number"),
        Some("http://localhost:9056/pay/pay-by-card-change-reference-number")
      )
    }

    "[PfTpes] should render the payment reference row correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfTpes.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(referenceRow, "Cyfeirnod", "XE123456789012", Some("Newid Cyfeirnod"), Some("http://localhost:9056/pay/pay-by-card-change-reference-number"))
    }

    "[PfMgd] should render the payment reference row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfMgd.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(
        referenceRow,
        "Reference number",
        "XE123456789012",
        Some("Change Reference number"),
        Some("http://localhost:9056/pay/pay-by-card-change-reference-number")
      )
    }

    "[PfMgd] should render the payment reference row correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfMgd.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(referenceRow, "Cyfeirnod", "XE123456789012", Some("Newid Cyfeirnod"), Some("http://localhost:9056/pay/pay-by-card-change-reference-number"))
    }

    "[PfGbPbRgDuty] should render the payment reference row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfGbPbRgDuty.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(
        referenceRow,
        "Reference number",
        "XE123456789012",
        Some("Change Reference number"),
        Some("http://localhost:9056/pay/pay-by-card-change-reference-number")
      )
    }

    "[PfGbPbRgDuty] should render the payment reference row correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfGbPbRgDuty.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(referenceRow, "Cyfeirnod", "XE123456789012", Some("Newid Cyfeirnod"), Some("http://localhost:9056/pay/pay-by-card-change-reference-number"))
    }

    "[PfTrust] should render the payment reference row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfTrust.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(
        referenceRow,
        "Reference number",
        "XE123456789012",
        Some("Change Reference number"),
        Some("http://localhost:9056/pay/pay-by-card-change-reference-number")
      )
    }

    "[PfTrust] should render the payment reference row correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfTrust.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(referenceRow, "Cyfeirnod", "XE123456789012", Some("Newid Cyfeirnod"), Some("http://localhost:9056/pay/pay-by-card-change-reference-number"))
    }

    "[PfPsAdmin] should render the payment reference row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfPsAdmin.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(
        referenceRow,
        "Reference number",
        "XE123456789012",
        Some("Change Reference number"),
        Some("http://localhost:9056/pay/pay-by-card-change-reference-number")
      )
    }

    "[PfPsAdmin] should render the payment reference row correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfPsAdmin.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(referenceRow, "Cyfeirnod", "XE123456789012", Some("Newid Cyfeirnod"), Some("http://localhost:9056/pay/pay-by-card-change-reference-number"))
    }

    "[PfOther] should render the payment reference row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfOther.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(
        referenceRow,
        "Payment reference",
        "XE123456789012",
        Some("Change Payment reference"),
        Some("http://localhost:9056/pay/pay-by-card-change-reference-number")
      )
    }

    "[PfOther] should render the payment reference row correctly in Welsh" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfOther.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequestWelsh())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(
        referenceRow,
        "Cyfeirnod y taliad",
        "XE123456789012",
        Some("Newid Cyfeirnod y taliad"),
        Some("http://localhost:9056/pay/pay-by-card-change-reference-number")
      )
    }

    "[StampTaxesOnShares] should not render the payment reference in first row" - {

      "when reference is a basket reference" in {
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.StampTaxesOnShares.journeyBeforeBeginWebPayment)
        val result       = systemUnderTest.renderPage(fakeRequest())
        val document     = Jsoup.parse(contentAsString(result))
        val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
        assertRowKeyIsNot(referenceRow, "Payment reference")
      }

      "when reference is not basket reference" in {
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.StampTaxesOnShares.journeyBeforeBeginWebpaymentNoBasketReference)
        val result       = systemUnderTest.renderPage(fakeRequest())
        val document     = Jsoup.parse(contentAsString(result))
        val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
        assertRowKeyIsNot(referenceRow, "Payment reference")
      }
    }

    "[PfStampTaxesOnShares] should render the payment reference row correctly" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfStampTaxesOnShares.journeyBeforeBeginWebPayment)
      val result       = systemUnderTest.renderPage(fakeRequest())
      val document     = Jsoup.parse(contentAsString(result))
      val referenceRow = document.select(".govuk-summary-list__row").asScala.toList.head
      assertRow(
        referenceRow,
        "Securities transfer charge reference",
        "XE123456789012",
        Some("Change Securities transfer charge reference"),
        Some("http://localhost:9056/pay/pay-by-card-change-reference-number")
      )
    }

    "sanity check for implemented origins" in {
      // remember to add the singular tests for reference rows as well as fdp if applicable, they are not covered in the implementedOrigins forall tests
      TestHelpers.implementedOrigins.size shouldBe 72 withClue "** This dummy test is here to remind you to update the tests above. Bump up the expected number when an origin is added to implemented origins **"
    }

  }

  private def assertRow(element: Element, keyText: String, valueText: String, actionText: Option[String], actionHref: Option[String]): Assertion = {
    element.select(".govuk-summary-list__key").text() shouldBe keyText
    element.select(".govuk-summary-list__value").text() shouldBe valueText

    actionText.fold {
      element.toString should not contain "Change"
      element.select(".govuk-summary-list__actions").asScala.size shouldBe 0 withClue "Expected No change links but there was one"
    }(content => element.select(".govuk-summary-list__actions").text() shouldBe content)

    actionHref.fold(element.select(".govuk-summary-list__actions").select("a").text() shouldBe "")(href =>
      element.select(".govuk-summary-list__actions").select("a").attr("href") shouldBe href
    )
  }

  private def assertRowKeyIsNot(element: Element, keyText: String): Assertion = {
    val key = element.select(".govuk-summary-list__key")
    key.size() shouldBe 1
    key.text().trim should not be keyText
  }

  "POST /check-your-details" - {

    "should redirect to the iframe page when there is an address in session" in {
      val expectedCardPaymentInitiatePaymentResponse = CardPaymentInitiatePaymentResponse("http://localhost:10155/this-would-be-iframe", "sometransactionref")
      CardPaymentStub.InitiatePayment.stubForInitiatePayment2xx(expectedCardPaymentInitiatePaymentResponse)
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)

      val result = systemUnderTest.submit(fakeRequest(TestJourneys.PfSa.journeyBeforeBeginWebPayment._id))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/pay-by-card/card-details?iframeUrl=http%3A%2F%2Flocalhost%3A10155%2Fthis-would-be-iframe")
    }

    "should redirect to iFrameUrl if PaymentStatus is Sent and there is an order present" in {
      def fakeRequestWithSentPaymentStatus(journeyId: JourneyId = TestJourneys.PfSa.journeyAfterBeginWebPayment._id): FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest().withSessionId().withEmailAndAddressInSession(cryptoService, journeyId)
      PayApiStub.stubForFindBySessionId2xx(
        TestJourneys.PfSa.journeyAfterBeginWebPayment.copy(order =
          Some(
            BarclaysOrder(
              transactionReference = TransactionReference("Some-transaction-ref"),
              iFrameUrl = Url("http://localhost:9975/barclays/pages/paypage.jsf/600e1342-0714-4989-ac6c-c11c745f1ce6"),
              cardCategory = None,
              commissionInPence = None,
              paidOn = None
            )
          )
        )
      )
      val result                                                                                                                                          = systemUnderTest.submit(fakeRequestWithSentPaymentStatus())
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(
        "/pay-by-card/card-details?iframeUrl=http%3A%2F%2Flocalhost%3A9975%2Fbarclays%2Fpages%2Fpaypage.jsf%2F600e1342-0714-4989-ac6c-c11c745f1ce6"
      )
    }

    "should redirect to the Address page if there is no Address in session" in {
      def fakeRequestWithoutAddressInSession: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSessionId()
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
      val result                                                                  = systemUnderTest.submit(fakeRequestWithoutAddressInSession)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/pay-by-card/address")
    }
  }

}
