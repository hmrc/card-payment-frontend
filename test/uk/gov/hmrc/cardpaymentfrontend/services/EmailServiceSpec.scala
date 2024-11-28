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

import play.api.test.FakeRequest
import uk.gov.hmrc.cardpaymentfrontend.models.email.{EmailParameters, EmailRequest}
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec
import uk.gov.hmrc.cardpaymentfrontend.testsupport.TestOps.FakeRequestOps
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.TestJourneys

class EmailServiceSpec extends ItSpec {

  val systemUnderTest: EmailService = app.injector.instanceOf[EmailService]

  "EmailService" - {

    val fakeGetRequest = FakeRequest("GET", "/").withSessionId()
    val fakeGetRequestInWelsh = fakeGetRequest.withLangWelsh()

    "buildEmailRequest" - {

      val testEmailParameters = EmailParameters(
        taxType          = "Self Assessment",
        taxReference     = "1234567895K",
        paymentReference = "Some-transaction-ref",
        amountPaid       = "12.34",
        commission       = None,
        totalPaid        = Some("12.34")
      )

      "Should return an EmailRequest" - {

        "request in english" in {
          val expectedResult: EmailRequest = EmailRequest(
            to         = List("joe_bloggs@gmail.com"),
            templateId = "payment_successful",
            parameters = testEmailParameters,
            force      = false
          )

          val result = systemUnderTest.buildEmailRequest(TestJourneys.PfSa.testPfSaJourneySuccessDebit, isEnglish = true)(fakeGetRequest)
          result shouldBe expectedResult
        }

        "request in welsh" in {
          val expectedResult: EmailRequest = EmailRequest(
            to         = List("joe_bloggs@gmail.com"),
            templateId = "payment_successful_cy",
            parameters = testEmailParameters,
            force      = false
          )

          val result = systemUnderTest.buildEmailRequest(TestJourneys.PfSa.testPfSaJourneySuccessDebit, isEnglish = false)(fakeGetRequest)
          result shouldBe expectedResult
        }

      }

    }

    "buildEmailParameters" - {
      "should return an EmailParameters" - {

        "when origin is PfSa" - {

          "debit in english" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "Self Assessment",
              taxReference     = "1234567895K",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = None,
              totalPaid        = Some("12.34")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.PfSa.testPfSaJourneySuccessDebit)(fakeGetRequest)
            result shouldBe expectedResult
          }

          "credit in english" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "Self Assessment",
              taxReference     = "1234567895K",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = Some("1.23"),
              totalPaid        = Some("13.57")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.PfSa.testPfSaJourneySuccessCredit)(fakeGetRequest)
            result shouldBe expectedResult
          }

          "debit in welsh" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "Hunanasesiad",
              taxReference     = "1234567895K",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = None,
              totalPaid        = Some("12.34")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.PfSa.testPfSaJourneySuccessDebit)(fakeGetRequestInWelsh)
            result shouldBe expectedResult
          }

          "credit in welsh" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "Hunanasesiad",
              taxReference     = "1234567895K",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = Some("1.23"),
              totalPaid        = Some("13.57")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.PfSa.testPfSaJourneySuccessCredit)(fakeGetRequestInWelsh)
            result shouldBe expectedResult
          }
        }

        "when origin is BtaSa" - {

          "debit in english" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "Self Assessment",
              taxReference     = "1234567895K",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = None,
              totalPaid        = Some("12.34")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.BtaSa.testBtaSaJourneySuccessDebit)(fakeGetRequest)
            result shouldBe expectedResult
          }

          "credit in english" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "Self Assessment",
              taxReference     = "1234567895K",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = Some("1.23"),
              totalPaid        = Some("13.57")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.BtaSa.testBtaSaJourneySuccessCredit)(fakeGetRequest)
            result shouldBe expectedResult
          }

          "debit in welsh" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "Hunanasesiad",
              taxReference     = "1234567895K",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = None,
              totalPaid        = Some("12.34")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.BtaSa.testBtaSaJourneySuccessDebit)(fakeGetRequestInWelsh)
            result shouldBe expectedResult
          }

          "credit in welsh" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "Hunanasesiad",
              taxReference     = "1234567895K",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = Some("1.23"),
              totalPaid        = Some("13.57")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.BtaSa.testBtaSaJourneySuccessCredit)(fakeGetRequestInWelsh)
            result shouldBe expectedResult
          }
        }
      }

    }

  }

}
