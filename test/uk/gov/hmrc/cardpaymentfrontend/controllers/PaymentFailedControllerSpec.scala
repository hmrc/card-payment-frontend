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
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import payapi.cardpaymentjourney.model.journey.{Journey, JourneySpecificData}
import payapi.corcommon.model.Origin
import payapi.corcommon.model.Origins.*
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.mvc.Http.Status
import uk.gov.hmrc.cardpaymentfrontend.forms.ChooseAPaymentMethodFormValues
import uk.gov.hmrc.cardpaymentfrontend.testsupport.TestOps.FakeRequestOps
import uk.gov.hmrc.cardpaymentfrontend.testsupport.stubs.PayApiStub
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.TestJourneys
import uk.gov.hmrc.cardpaymentfrontend.testsupport.{ItSpec, TestHelpers}

import scala.jdk.CollectionConverters.ListHasAsScala

class PaymentFailedControllerSpec extends ItSpec {

  private val systemUnderTest: PaymentFailedController = app.injector.instanceOf[PaymentFailedController]

  "PaymentFailedController" - {

    "GET /payment-failed" - {

      val fakeGetRequest: FakeRequest[AnyContentAsEmpty.type]        = FakeRequest("GET", "/payment-failed").withSessionId()
      val fakeGetRequestInWelsh: FakeRequest[AnyContentAsEmpty.type] = fakeGetRequest.withLangWelsh()

      "should render the correct page and content" - {

        TestHelpers.implementedOrigins.foreach { origin =>

          val testJourney: Journey[JourneySpecificData] = TestHelpers.deriveTestDataFromOrigin(origin).journeyAfterFailWebPayment

          s"in english for origin: ${origin.entryName}" in {
            PayApiStub.stubForFindBySessionId2xx(testJourney)
            val result   = systemUnderTest.renderPage(fakeGetRequest)
            status(result) shouldBe Status.OK
            val document = Jsoup.parse(contentAsString(result))
            assertionsForOrigin(origin = testJourney.origin, document = document, welshTest = false)
          }

          s"in welsh for origin ${origin.entryName}" in {
            PayApiStub.stubForFindBySessionId2xx(testJourney)
            val result   = systemUnderTest.renderPage(fakeGetRequestInWelsh)
            status(result) shouldBe Status.OK
            val document = Jsoup.parse(contentAsString(result))
            assertionsForOrigin(origin = testJourney.origin, document = document, welshTest = true)
          }
        }
      }

    }

    "POST /payment-failed" - {
      "should redirect to /email-address-journey-retry when try again option is submitted" in {
        val fakeGetRequest: FakeRequest[AnyContentAsFormUrlEncoded] =
          FakeRequest("POST", "/payment-failed").withSessionId().withFormUrlEncodedBody(("payment_method", ChooseAPaymentMethodFormValues.TryAgain.entryName))
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfOther.journeyAfterFailWebPayment)
        val result                                                  = systemUnderTest.submit(fakeGetRequest)
        redirectLocation(result) shouldBe Some("/pay-by-card/email-address-journey-retry")
      }

      "should redirect to /start-open-banking when open banking option is submitted" in {
        val fakeGetRequest: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest("POST", "/payment-failed")
          .withSessionId()
          .withFormUrlEncodedBody(("payment_method", ChooseAPaymentMethodFormValues.OpenBanking.entryName))
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyAfterFailWebPayment)
        val result                                                  = systemUnderTest.submit(fakeGetRequest)
        redirectLocation(result) shouldBe Some("/pay-by-card/start-open-banking")
      }

      "Should show the correct error Title content in English" in {
        val fakeGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("POST", "/payment-failed").withSessionId()
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyAfterFailWebPayment)
        val result                                              = systemUnderTest.submit(fakeGetRequest)
        val document                                            = Jsoup.parse(contentAsString(result))
        document.title() shouldBe "Error: Payment failed - Pay your Self Assessment - GOV.UK"
      }

      "Should show the correct error Title content in Welsh" in {
        val fakeGetRequestInWelsh: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/payment-failed").withSessionId().withLangWelsh()
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyAfterFailWebPayment)
        val result                                                     = systemUnderTest.submit(fakeGetRequestInWelsh)
        val document                                                   = Jsoup.parse(contentAsString(result))
        document.title() shouldBe "Gwall: Taliad wedi methu - Talu eich Hunanasesiad - GOV.UK"
      }

      "Should show the correct error content in English - BadRequest" in {
        val fakeGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("POST", "/payment-failed").withSessionId()
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyAfterFailWebPayment)
        val result                                              = systemUnderTest.submit(fakeGetRequest)
        status(result) shouldBe 400
        val document                                            = Jsoup.parse(contentAsString(result))
        val errorSummary                                        = document.select(".govuk-error-summary")
        errorSummary.select("h2").text() shouldBe "There is a problem"
        val errorSummaryList                                    = errorSummary.select(".govuk-error-summary__list").select("li").asScala.toList
        errorSummaryList.size shouldBe 1
        errorSummaryList.map(_.text()) shouldBe List("Select how you want to pay")
        document.select(".govuk-error-message").text() shouldBe "Error: Select how you want to pay"
      }

      "Should show the correct error content in Welsh - BadRequest" in {
        val fakeGetRequestInWelsh: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/payment-failed").withSessionId().withLangWelsh()
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyAfterFailWebPayment)
        val result                                                     = systemUnderTest.submit(fakeGetRequestInWelsh)
        status(result) shouldBe 400
        val document: Document                                         = Jsoup.parse(contentAsString(result))
        val errorSummary                                               = document.select(".govuk-error-summary")
        errorSummary.select("h2").text() shouldBe "Mae problem wedi codi"
        val errorSummaryList                                           = errorSummary.select(".govuk-error-summary__list").select("li").asScala.toList
        errorSummaryList.size shouldBe 1
        errorSummaryList.map(_.text()) shouldBe List("Dewiswch sut yr ydych am dalu")
        document.select(".govuk-error-message").text() shouldBe "Gwall: Dewiswch sut yr ydych am dalu"
      }

      "should return BadRequest when value not in ChooseAPaymentMethodFormValues is submitted" in {
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyAfterFailWebPayment)
        val result = systemUnderTest.submit(FakeRequest("POST", "/payment-failed").withSessionId().withFormUrlEncodedBody("payment-method" -> "IAmInValid"))
        status(result) shouldBe 400
      }
    }
  }

  private def commonAssertions(document: Document, welshTest: Boolean): Assertion = {
    document.select("h1").html() shouldBe (if (!welshTest) "Payment failed" else "Taliad wedi methu")
    document.select("#p1").html() shouldBe (if (!welshTest) "No payment has been taken from your card." else "Nid oes taliad wedi’i dynnu o’ch cerdyn.")
  }

  private def noOpenBankingSupportedAssertion(document: Document, welshTest: Boolean): Assertion = {
    commonAssertions(document, welshTest)
    document.select("#p2").html() shouldBe (if (!welshTest) "The payment may have failed if:" else "Mae’n bosibl bod y taliad wedi methi oherwydd:")
    document.select("#bullet-list").attr("class") shouldBe "govuk-list--bullet govuk-!-static-padding-left-7"
    document.select("#bullet1").html() shouldBe (if (!welshTest) "there are not enough funds in your account" else "nid oes yna ddigon o arian yn eich cyfrif")
    document.select("#bullet2").html() shouldBe (if (!welshTest) "you entered invalid or expired card details"
                                                 else "rydych wedi nodi manylion cerdyn sy’n annilys neu sydd wedi dod i ben")
    document.select("#bullet3").html() shouldBe (if (!welshTest) "the address you gave does not match the one your card issuer has"
                                                 else "nid yw’r cyfeiriad a roesoch i ni’n cyd-fynd â’r un sydd gan ddosbarthwr eich cerdyn")
    document.select("#check-again").html() shouldBe (if (!welshTest) "Check the details you entered are correct or try a different card."
                                                     else "Gwiriwch fod y manylion a nodwyd gennych yn gywir neu rhowch gynnig ar gerdyn gwahanol.")
    val button = document.select("#next")
    button.`val`() shouldBe "TryAgain"
    button.html() shouldBe (if (!welshTest) "Check details and try again" else "Gwiriwch y manylion a rhowch gynnig arall arni")
  } withClue "expected no open banking form"

  private def openBankingSupportedAssertion(document: Document, welshTest: Boolean): Assertion = {
    commonAssertions(document, welshTest)
    val form             = document.select("form")
    form.select("legend").html() shouldBe (if (!welshTest) "What do you want to do?" else "Beth hoffech chi ei wneud?")
    form.select(".govuk-radios__item").size() shouldBe 2
    val radioItems       = form.select(".govuk-radios__item").asScala.toList
    val openBankingRadio = radioItems.headOption
    openBankingRadio.map(_.text()) shouldBe (if (!welshTest) Some("Approve a payment to come straight from my bank account")
                                             else Some("Cymeradwyo taliad i fynd yn syth o’m cyfrif banc"))
    openBankingRadio.map(_.select("input").`val`()) shouldBe Some("OpenBanking")
    val tryAgainRadio    = radioItems.lastOption
    tryAgainRadio.map(_.text()) shouldBe (if (!welshTest) Some("Try card payment again") else Some("Rhowch gynnig arall ar dalu drwy gerdyn"))
    tryAgainRadio.map(_.select("input").`val`()) shouldBe Some("TryAgain")
    document.select("#next").text() shouldBe (if (!welshTest) "Continue" else "Yn eich blaen")
  } withClue "expected open banking form"

  private def passengersAssertion(document: Document, welshTest: Boolean): Assertion = {
    commonAssertions(document, welshTest)
    document.select("#p2").html() shouldBe (if (!welshTest) "The payment may have failed if:" else "Mae’n bosibl bod y taliad wedi methi oherwydd:")
    document.select("#bullet-list").attr("class") shouldBe "govuk-list--bullet govuk-!-static-padding-left-7"
    document.select("#bullet1").html() shouldBe (if (!welshTest) "there are not enough funds in your account" else "nid oes yna ddigon o arian yn eich cyfrif")
    document.select("#bullet2").html() shouldBe (if (!welshTest) "you entered invalid or expired card details"
                                                 else "rydych wedi nodi manylion cerdyn sy’n annilys neu sydd wedi dod i ben")
    document.select("#check-again").html() shouldBe (if (!welshTest) "Check the details you entered are correct or try a different card."
                                                     else "Gwiriwch fod y manylion a nodwyd gennych yn gywir neu rhowch gynnig ar gerdyn gwahanol.")
  } withClue "expected no open banking form"

  private def assertionsForOrigin(origin: Origin, document: Document, welshTest: Boolean): Assertion = {
    origin match {
      case PfSa | BtaSa | PtaSa | PfVat | PfCt | PfEpayeNi | PfEpayeLpp | PfEpayeSeta | PfEpayeLateCis | PfEpayeP11d | PfSdlt | PfCds | PtaP800 | PfPsAdmin |
          AppSa | BtaVat | BtaEpayeBill | BtaEpayePenalty | BtaEpayeInterest | BtaEpayeGeneral | BtaClass1aNi | BtaCt | BtaSdil | DdVat | DdSdil | VcVatReturn |
          VcVatOther | Amls | Ppt | PfPpt | PfMgd | PfGbPbRgDuty | PfSdil | PfSimpleAssessment | PtaSimpleAssessment | WcSimpleAssessment | PfTpes |
          CapitalGainsTax | EconomicCrimeLevy | PfEconomicCrimeLevy | PfChildBenefitRepayments | NiEuVatOss | PfNiEuVatOss | NiEuVatIoss | PfNiEuVatIoss |
          PfAmls | PfTrust | AlcoholDuty | PfAlcoholDuty | VatC2c | PfVatC2c | WcSa | WcCt | WcVat | WcClass1aNi | WcEpayeLpp | WcEpayeNi | WcEpayeLateCis |
          WcEpayeSeta | WcSdlt | WcChildBenefitRepayments =>
        openBankingSupportedAssertion(document, welshTest)

      case ItSa                                                                                                 => noOpenBankingSupportedAssertion(document, welshTest) // will be changed to openBankingSupportedAssertion as part of OPS-6528
      case WcXref | PfOther | PfP800 | PfJobRetentionScheme | JrsJobRetentionScheme | AppSimpleAssessment | Mib =>
        noOpenBankingSupportedAssertion(document, welshTest)

      case BcPngr => passengersAssertion(document, welshTest)

      case PfImportedVehicles | PfAted | PfCdsDeferment | PfClass2Ni | PfInsurancePremium | Parcels | PfCdsCash | PfSpiritDrinks | PfInheritanceTax |
          PfClass3Ni | PfWineAndCider | PfBioFuels | PfAirPass | PfBeerDuty | PfGamingOrBingoDuty | PfLandfillTax | PfAggregatesLevy | PfClimateChangeLevy |
          PtaClass3Ni | `3psSa` | `3psVat` | PfPillar2 | Pillar2 | WcClass2Ni =>
        throw new MatchError("No card journey expected to be supported for this origin, why is it being tested?")
    }
  }
}
