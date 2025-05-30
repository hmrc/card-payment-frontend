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

package uk.gov.hmrc.cardpaymentfrontend.services

import payapi.cardpaymentjourney.model.journey.Url
import play.api.test.FakeRequest
import uk.gov.hmrc.cardpaymentfrontend.models.paymentssurvey.{AuditOptions, PaymentSurveyJourneyRequest, SurveyBannerTitle, SurveyContentOptions}
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.{TestJourneys, TestPaymentsSurveyData}
import uk.gov.hmrc.cardpaymentfrontend.testsupport.TestOps.FakeRequestOps
import uk.gov.hmrc.cardpaymentfrontend.testsupport.stubs.PaymentsSurveyStub

class PaymentsSurveyServiceSpec extends ItSpec {

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
      }
    }
  }

}
