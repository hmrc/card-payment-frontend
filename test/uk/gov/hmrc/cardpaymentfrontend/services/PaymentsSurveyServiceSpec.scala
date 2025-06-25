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

package uk.gov.hmrc.cardpaymentfrontend.services

import org.scalatest.prop.TableDrivenPropertyChecks
import payapi.cardpaymentjourney.model.journey.Url
import play.api.test.FakeRequest
import uk.gov.hmrc.cardpaymentfrontend.models.paymentssurvey.{AuditOptions, PaymentSurveyJourneyRequest, SurveyBannerTitle, SurveyContentOptions}
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.{TestJourneys, TestPaymentsSurveyData}
import uk.gov.hmrc.cardpaymentfrontend.testsupport.TestOps.FakeRequestOps
import uk.gov.hmrc.cardpaymentfrontend.testsupport.stubs.PaymentsSurveyStub

class PaymentsSurveyServiceSpec extends ItSpec with TableDrivenPropertyChecks {

  val systemUnderTest: PaymentsSurveyService = app.injector.instanceOf[PaymentsSurveyService]

  "PaymentsSurveyService" - {
    "startPaySurvey" - {
      "return a future Url given call to PaymentsSurvey succeeds" in {
        PaymentsSurveyStub.stubForStartJourney2xx(TestPaymentsSurveyData.ssJResponse)
        val result = systemUnderTest.startPaySurvey(TestJourneys.PfSa.journeyAfterSucceedDebitWebPayment)(FakeRequest())
        result.futureValue shouldBe Url("http://survey-redirect-url.com")
      }
      "fail when call to PaymentsSurvey fails" in {
        PaymentsSurveyStub.stubForStartJourney5xx()
        val result = systemUnderTest.startPaySurvey(TestJourneys.PfSa.journeyAfterSucceedDebitWebPayment)(FakeRequest())
        result.failed.futureValue.getMessage shouldBe s"POST of 'http://localhost:${wireMockPort.toString}/payments-survey/journey/start' returned 503. Response body: ''"
      }
    }

    "makeSsjJourneyRequest" - {
      val loggedInFakeRequest = FakeRequest().withAuthSession()
      val loggedOutFakeRequest = FakeRequest()
      "correctly build a PaymentSurveyJourneyRequest" - {
        "for PfSa" in {
          val expectedPaymentSurveyJourneyRequest = PaymentSurveyJourneyRequest(
            origin         = "PfSa",
            returnMsg      = "Skip survey",
            returnHref     = "https://www.gov.uk/government/organisations/hm-revenue-customs",
            auditName      = "self-assessment",
            audit          = AuditOptions(
              userType  = "LoggedOut",
              journey   = Some("Successful"),
              orderId   = Some("1234567895K"),
              liability = Some("self-assessment")
            ),
            contentOptions = SurveyContentOptions(
              isWelshSupported = true,
              title            = SurveyBannerTitle(
                englishValue = "Pay your Self Assessment", welshValue = Some("Talu eich Hunanasesiad")
              )
            )
          )
          val result = systemUnderTest.makeSsjJourneyRequest(TestJourneys.PfSa.journeyAfterSucceedDebitWebPayment)(loggedOutFakeRequest)
          result shouldBe expectedPaymentSurveyJourneyRequest
        }

        "for BtaSa" in {
          val expectedPaymentSurveyJourneyRequest = PaymentSurveyJourneyRequest(
            origin         = "BtaSa",
            returnMsg      = "Skip survey, return to business tax account",
            returnHref     = "/business-account",
            auditName      = "self-assessment",
            audit          = AuditOptions(
              userType  = "LoggedIn",
              journey   = Some("Successful"),
              orderId   = Some("1234567895K"),
              liability = Some("self-assessment")
            ),
            contentOptions = SurveyContentOptions(
              isWelshSupported = true,
              title            = SurveyBannerTitle(
                englishValue = "Pay your Self Assessment", welshValue = Some("Talu eich Hunanasesiad")
              )
            )
          )
          val result = systemUnderTest.makeSsjJourneyRequest(TestJourneys.BtaSa.journeyAfterSucceedDebitWebPayment)(loggedInFakeRequest)
          result shouldBe expectedPaymentSurveyJourneyRequest
        }

        "for PtaSa" in {
          val expectedPaymentSurveyJourneyRequest = PaymentSurveyJourneyRequest(
            origin         = "PtaSa",
            returnMsg      = "Skip survey, return to personal tax account",
            returnHref     = "/personal-account",
            auditName      = "self-assessment",
            audit          = AuditOptions(
              userType  = "LoggedIn",
              journey   = Some("Successful"),
              orderId   = Some("1234567895K"),
              liability = Some("self-assessment")
            ),
            contentOptions = SurveyContentOptions(
              isWelshSupported = true,
              title            = SurveyBannerTitle(
                englishValue = "Pay your Self Assessment", welshValue = Some("Talu eich Hunanasesiad")
              )
            )
          )
          val result = systemUnderTest.makeSsjJourneyRequest(TestJourneys.PtaSa.journeyAfterSucceedDebitWebPayment)(loggedInFakeRequest)
          result shouldBe expectedPaymentSurveyJourneyRequest
        }

        "for ItSa" in {
          val expectedPaymentSurveyJourneyRequest = PaymentSurveyJourneyRequest(
            origin         = "ItSa",
            returnMsg      = "Skip survey",
            returnHref     = "https://www.gov.uk/government/organisations/hm-revenue-customs",
            auditName      = "self-assessment",
            audit          = AuditOptions(
              userType  = "LoggedIn",
              journey   = Some("Successful"),
              orderId   = Some("1234567895K"),
              liability = Some("self-assessment")
            ),
            contentOptions = SurveyContentOptions(
              isWelshSupported = true,
              title            = SurveyBannerTitle(
                englishValue = "Pay your Self Assessment", welshValue = Some("Talu eich Hunanasesiad")
              )
            )
          )
          val result = systemUnderTest.makeSsjJourneyRequest(TestJourneys.ItSa.journeyAfterSucceedDebitWebPayment)(loggedInFakeRequest)
          result shouldBe expectedPaymentSurveyJourneyRequest
        }

        "for AlcoholDuty" in {
          val expectedPaymentSurveyJourneyRequest = PaymentSurveyJourneyRequest(
            origin         = "AlcoholDuty",
            returnMsg      = "Skip survey",
            returnHref     = "https://www.gov.uk/government/organisations/hm-revenue-customs",
            auditName      = "alcohol-duty",
            audit          = AuditOptions(
              userType  = "LoggedIn",
              journey   = Some("Successful"),
              orderId   = Some("XMADP0123456789"),
              liability = Some("alcohol-duty")
            ),
            contentOptions = SurveyContentOptions(
              isWelshSupported = true,
              title            = SurveyBannerTitle(
                englishValue = "Pay your Alcohol Duty", welshValue = Some("Talu’ch Toll Alcohol")
              )
            )
          )
          val result = systemUnderTest.makeSsjJourneyRequest(TestJourneys.AlcoholDuty.journeyAfterSucceedDebitWebPayment)(loggedInFakeRequest)
          result shouldBe expectedPaymentSurveyJourneyRequest
        }

        "for PfAlcoholDuty" in {
          val expectedPaymentSurveyJourneyRequest = PaymentSurveyJourneyRequest(
            origin         = "PfAlcoholDuty",
            returnMsg      = "Skip survey",
            returnHref     = "https://www.gov.uk/government/organisations/hm-revenue-customs",
            auditName      = "alcohol-duty",
            audit          = AuditOptions(
              userType  = "LoggedOut",
              journey   = Some("Successful"),
              orderId   = Some("XMADP0123456789"),
              liability = Some("alcohol-duty")
            ),
            contentOptions = SurveyContentOptions(
              isWelshSupported = true,
              title            = SurveyBannerTitle(
                englishValue = "Pay your Alcohol Duty", welshValue = Some("Talu’ch Toll Alcohol")
              )
            )
          )
          val result = systemUnderTest.makeSsjJourneyRequest(TestJourneys.PfAlcoholDuty.journeyAfterSucceedDebitWebPayment)(loggedOutFakeRequest)
          result shouldBe expectedPaymentSurveyJourneyRequest
        }

        "for BtaCt" in {
          val expectedPaymentSurveyJourneyRequest = PaymentSurveyJourneyRequest(
            origin         = "BtaCt",
            returnMsg      = "Skip survey, return to business tax account",
            returnHref     = "/business-account",
            auditName      = "corporation-tax",
            audit          = AuditOptions(
              userType  = "LoggedIn",
              journey   = Some("Successful"),
              orderId   = Some("1097172564A00101A"),
              liability = Some("corporation-tax")
            ),
            contentOptions = SurveyContentOptions(
              isWelshSupported = true,
              title            = SurveyBannerTitle(
                englishValue = "Pay your Corporation Tax", welshValue = Some("Talu eich Treth Gorfforaeth")
              )
            )
          )
          val result = systemUnderTest.makeSsjJourneyRequest(TestJourneys.BtaCt.journeyAfterSucceedDebitWebPayment)(loggedInFakeRequest)
          result shouldBe expectedPaymentSurveyJourneyRequest
        }

        "for PfCt" in {
          val expectedPaymentSurveyJourneyRequest = PaymentSurveyJourneyRequest(
            origin         = "PfCt",
            returnMsg      = "Skip survey",
            returnHref     = "https://www.gov.uk/government/organisations/hm-revenue-customs",
            auditName      = "corporation-tax",
            audit          = AuditOptions(
              userType  = "LoggedOut",
              journey   = Some("Successful"),
              orderId   = Some("1097172564A00101A"),
              liability = Some("corporation-tax")
            ),
            contentOptions = SurveyContentOptions(
              isWelshSupported = true,
              title            = SurveyBannerTitle(
                englishValue = "Pay your Corporation Tax", welshValue = Some("Talu eich Treth Gorfforaeth")
              )
            )
          )
          val result = systemUnderTest.makeSsjJourneyRequest(TestJourneys.PfCt.journeyAfterSucceedDebitWebPayment)(loggedOutFakeRequest)
          result shouldBe expectedPaymentSurveyJourneyRequest
        }

        "for PfEpayeLpp" in {
          val expectedPaymentSurveyJourneyRequest = PaymentSurveyJourneyRequest(
            origin         = "PfEpayeLpp",
            returnMsg      = "Skip survey",
            returnHref     = "https://www.gov.uk/government/organisations/hm-revenue-customs",
            auditName      = "paye-lpp",
            audit          = AuditOptions(
              userType  = "LoggedOut",
              journey   = Some("Successful"),
              orderId   = Some("XE123456789012"),
              liability = Some("paye-lpp")
            ),
            contentOptions = SurveyContentOptions(
              isWelshSupported = true,
              title            = SurveyBannerTitle(
                englishValue = "Pay your PAYE late payment or filing penalty", welshValue = Some("Talu’ch cosb am dalu neu gyflwyno TWE yn hwyr")
              )
            )
          )
          val result = systemUnderTest.makeSsjJourneyRequest(TestJourneys.PfEpayeLpp.journeyAfterSucceedDebitWebPayment)(loggedOutFakeRequest)
          result shouldBe expectedPaymentSurveyJourneyRequest
        }

        "for PfEpayeLateCis" in {
          val expectedPaymentSurveyJourneyRequest = PaymentSurveyJourneyRequest(
            origin         = "PfEpayeLateCis",
            returnMsg      = "Skip survey",
            returnHref     = "https://www.gov.uk/government/organisations/hm-revenue-customs",
            auditName      = "paye-late-cis",
            audit          = AuditOptions(
              userType  = "LoggedOut",
              journey   = Some("Successful"),
              orderId   = Some("XE123456789012"),
              liability = Some("paye-late-cis")
            ),
            contentOptions = SurveyContentOptions(
              isWelshSupported = true,
              title            = SurveyBannerTitle(
                englishValue = "Pay your Construction Industry Scheme penalty", welshValue = Some("Talwch eich cosb - Cynllun y Diwydiant Adeiladu")
              )
            )
          )
          val result = systemUnderTest.makeSsjJourneyRequest(TestJourneys.PfEpayeLateCis.journeyAfterSucceedDebitWebPayment)(loggedOutFakeRequest)
          result shouldBe expectedPaymentSurveyJourneyRequest
        }

        "for PfEpayeNi" in {
          val expectedPaymentSurveyJourneyRequest = PaymentSurveyJourneyRequest(
            origin         = "PfEpayeNi",
            returnMsg      = "Skip survey",
            returnHref     = "https://www.gov.uk/government/organisations/hm-revenue-customs",
            auditName      = "paye-ni",
            audit          = AuditOptions(
              userType  = "LoggedOut",
              journey   = Some("Successful"),
              orderId   = Some("123PH456789002503"),
              liability = Some("paye-ni")
            ),
            contentOptions = SurveyContentOptions(
              isWelshSupported = true,
              title            = SurveyBannerTitle(
                englishValue = "Pay your employers’ PAYE and National Insurance", welshValue = Some("Talwch eich TWE a’ch Yswiriant Gwladol y cyflogwr")
              )
            )
          )
          val result = systemUnderTest.makeSsjJourneyRequest(TestJourneys.PfEpayeNi.journeyAfterSucceedDebitWebPayment)(loggedOutFakeRequest)
          result shouldBe expectedPaymentSurveyJourneyRequest
        }

        "for PfEpayeP11d" in {
          val expectedPaymentSurveyJourneyRequest = PaymentSurveyJourneyRequest(
            origin         = "PfEpayeP11d",
            returnMsg      = "Skip survey",
            returnHref     = "https://www.gov.uk/government/organisations/hm-revenue-customs",
            auditName      = "paye-p11d",
            audit          = AuditOptions(
              userType  = "LoggedOut",
              journey   = Some("Successful"),
              orderId   = Some("123PH456789002513"),
              liability = Some("paye-p11d")
            ),
            contentOptions = SurveyContentOptions(
              isWelshSupported = true,
              title            = SurveyBannerTitle(
                englishValue = "Pay your employers’ Class 1A National Insurance (P11D bill)", welshValue = Some("Talu’ch Yswiriant Gwladol Dosbarth 1A y cyflogwr (bil P11D)")
              )
            )
          )
          val result = systemUnderTest.makeSsjJourneyRequest(TestJourneys.PfEpayeP11d.journeyAfterSucceedDebitWebPayment)(loggedOutFakeRequest)
          result shouldBe expectedPaymentSurveyJourneyRequest
        }

        "for PfEpayeSeta" in {
          val expectedPaymentSurveyJourneyRequest = PaymentSurveyJourneyRequest(
            origin         = "PfEpayeSeta",
            returnMsg      = "Skip survey",
            returnHref     = "https://www.gov.uk/government/organisations/hm-revenue-customs",
            auditName      = "paye-seta",
            audit          = AuditOptions(
              userType  = "LoggedOut",
              journey   = Some("Successful"),
              orderId   = Some("XA123456789012"),
              liability = Some("paye-seta")
            ),
            contentOptions = SurveyContentOptions(
              isWelshSupported = true,
              title            = SurveyBannerTitle(
                englishValue = "Pay your PAYE Settlement Agreement", welshValue = Some("Talwch eich Cytundeb Setliad TWE y cyflogwr")
              )
            )
          )
          val result = systemUnderTest.makeSsjJourneyRequest(TestJourneys.PfEpayeSeta.journeyAfterSucceedDebitWebPayment)(loggedOutFakeRequest)
          result shouldBe expectedPaymentSurveyJourneyRequest
        }

        "for PfVat" in {
          val expectedPaymentSurveyJourneyRequest = PaymentSurveyJourneyRequest(
            origin         = "PfVat",
            returnMsg      = "Skip survey",
            returnHref     = "https://www.gov.uk/government/organisations/hm-revenue-customs",
            auditName      = "vat",
            audit          = AuditOptions(
              userType  = "LoggedOut",
              journey   = Some("Successful"),
              orderId   = Some("999964805"),
              liability = Some("vat")
            ),
            contentOptions = SurveyContentOptions(
              isWelshSupported = true,
              title            = SurveyBannerTitle(
                englishValue = "Pay your VAT", welshValue = Some("Talu eich TAW")
              )
            )
          )
          val result = systemUnderTest.makeSsjJourneyRequest(TestJourneys.PfVat.journeyAfterSucceedDebitWebPayment)(loggedOutFakeRequest)
          result shouldBe expectedPaymentSurveyJourneyRequest
        }

        "for BtaVat" in {
          val expectedPaymentSurveyJourneyRequest = PaymentSurveyJourneyRequest(
            origin         = "BtaVat",
            returnMsg      = "Skip survey, return to business tax account",
            returnHref     = "/business-account",
            auditName      = "vat",
            audit          = AuditOptions(
              userType  = "LoggedIn",
              journey   = Some("Successful"),
              orderId   = Some("999964805"),
              liability = Some("vat")
            ),
            contentOptions = SurveyContentOptions(
              isWelshSupported = true,
              title            = SurveyBannerTitle(
                englishValue = "Pay your VAT", welshValue = Some("Talu eich TAW")
              )
            )
          )
          val result = systemUnderTest.makeSsjJourneyRequest(TestJourneys.BtaVat.journeyAfterSucceedDebitWebPayment)(loggedInFakeRequest)
          result shouldBe expectedPaymentSurveyJourneyRequest
        }

        "for VcVatReturn" in {
          val expectedPaymentSurveyJourneyRequest = PaymentSurveyJourneyRequest(
            origin         = "VcVatReturn",
            returnMsg      = "Skip survey, return to business tax account",
            returnHref     = "/business-account",
            auditName      = "vat",
            audit          = AuditOptions(
              userType  = "LoggedIn",
              journey   = Some("Successful"),
              orderId   = Some("999964805"),
              liability = Some("vat")
            ),
            contentOptions = SurveyContentOptions(
              isWelshSupported = true,
              title            = SurveyBannerTitle(
                englishValue = "Business tax account", welshValue = Some("Cyfrif treth busnes")
              )
            )
          )
          val result = systemUnderTest.makeSsjJourneyRequest(TestJourneys.VcVatReturn.journeyAfterSucceedDebitWebPayment)(loggedInFakeRequest)
          result shouldBe expectedPaymentSurveyJourneyRequest
        }

        "for VcVatOther" in {
          val expectedPaymentSurveyJourneyRequest = PaymentSurveyJourneyRequest(
            origin         = "VcVatOther",
            returnMsg      = "Skip survey, return to business tax account",
            returnHref     = "/business-account",
            auditName      = "vat",
            audit          = AuditOptions(
              userType  = "LoggedIn",
              journey   = Some("Successful"),
              orderId   = Some("999964805"),
              liability = Some("vat")
            ),
            contentOptions = SurveyContentOptions(
              isWelshSupported = true,
              title            = SurveyBannerTitle(
                englishValue = "Business tax account", welshValue = Some("Cyfrif treth busnes")
              )
            )
          )
          val result = systemUnderTest.makeSsjJourneyRequest(TestJourneys.VcVatOther.journeyAfterSucceedDebitWebPayment)(loggedInFakeRequest)
          result shouldBe expectedPaymentSurveyJourneyRequest
        }

        "for BtaEpayeBill" in {
          val expectedPaymentSurveyJourneyRequest = PaymentSurveyJourneyRequest(
            origin         = "BtaEpayeBill",
            returnMsg      = "Skip survey, return to business tax account",
            returnHref     = "/business-account",
            auditName      = "epaye",
            audit          = AuditOptions(
              userType  = "LoggedIn",
              journey   = Some("Successful"),
              orderId   = Some("123PH456789002702"),
              liability = Some("epaye")
            ),
            contentOptions = SurveyContentOptions(
              isWelshSupported = true,
              title            = SurveyBannerTitle(
                englishValue = "Pay your employers’ PAYE and National Insurance", welshValue = Some("Talwch eich TWE a’ch Yswiriant Gwladol y cyflogwr")
              )
            )
          )
          val result = systemUnderTest.makeSsjJourneyRequest(TestJourneys.BtaEpayeBill.journeyAfterSucceedDebitWebPayment)(loggedInFakeRequest)
          result shouldBe expectedPaymentSurveyJourneyRequest
        }

        "for BtaEpayePenalty" in {
          val expectedPaymentSurveyJourneyRequest = PaymentSurveyJourneyRequest(
            origin         = "BtaEpayePenalty",
            returnMsg      = "Skip survey, return to business tax account",
            returnHref     = "/business-account",
            auditName      = "epaye",
            audit          = AuditOptions(
              userType  = "LoggedIn",
              journey   = Some("Successful"),
              orderId   = Some("123PH45678900"),
              liability = Some("epaye")
            ),
            contentOptions = SurveyContentOptions(
              isWelshSupported = true,
              title            = SurveyBannerTitle(
                englishValue = "Pay your PAYE late payment or filing penalty", welshValue = Some("Talu’ch cosb am dalu neu gyflwyno TWE yn hwyr")
              )
            )
          )
          val result = systemUnderTest.makeSsjJourneyRequest(TestJourneys.BtaEpayePenalty.journeyAfterSucceedDebitWebPayment)(loggedInFakeRequest)
          result shouldBe expectedPaymentSurveyJourneyRequest
        }

        "for BtaEpayeInterest" in {
          val expectedPaymentSurveyJourneyRequest = PaymentSurveyJourneyRequest(
            origin         = "BtaEpayeInterest",
            returnMsg      = "Skip survey, return to business tax account",
            returnHref     = "/business-account",
            auditName      = "epaye",
            audit          = AuditOptions(
              userType  = "LoggedIn",
              journey   = Some("Successful"),
              orderId   = Some("X1234567890123"),
              liability = Some("epaye")
            ),
            contentOptions = SurveyContentOptions(
              isWelshSupported = true,
              title            = SurveyBannerTitle(
                englishValue = "Pay your employers’ PAYE and National Insurance", welshValue = Some("Talwch eich TWE a’ch Yswiriant Gwladol y cyflogwr")
              )
            )
          )
          val result = systemUnderTest.makeSsjJourneyRequest(TestJourneys.BtaEpayeInterest.journeyAfterSucceedDebitWebPayment)(loggedInFakeRequest)
          result shouldBe expectedPaymentSurveyJourneyRequest
        }

        "for BtaEpayeGeneral" in {
          val expectedPaymentSurveyJourneyRequest = PaymentSurveyJourneyRequest(
            origin         = "BtaEpayeGeneral",
            returnMsg      = "Skip survey, return to business tax account",
            returnHref     = "/business-account",
            auditName      = "epaye",
            audit          = AuditOptions(
              userType  = "LoggedIn",
              journey   = Some("Successful"),
              orderId   = Some("123PH456789002702"),
              liability = Some("epaye")
            ),
            contentOptions = SurveyContentOptions(
              isWelshSupported = true,
              title            = SurveyBannerTitle(
                englishValue = "Pay your employers’ PAYE and National Insurance", welshValue = Some("Talwch eich TWE a’ch Yswiriant Gwladol y cyflogwr")
              )
            )
          )
          val result = systemUnderTest.makeSsjJourneyRequest(TestJourneys.BtaEpayeGeneral.journeyAfterSucceedDebitWebPayment)(loggedInFakeRequest)
          result shouldBe expectedPaymentSurveyJourneyRequest
        }

      }
    }
  }

}
