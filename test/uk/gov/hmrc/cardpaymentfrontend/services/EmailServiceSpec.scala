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
import uk.gov.hmrc.cardpaymentfrontend.models.EmailAddress
import uk.gov.hmrc.cardpaymentfrontend.models.email.{EmailParameters, EmailRequest}
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec
import uk.gov.hmrc.cardpaymentfrontend.testsupport.TestOps.FakeRequestOps
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.TestJourneys
import uk.gov.hmrc.http.HeaderCarrier

class EmailServiceSpec extends ItSpec {

  val systemUnderTest: EmailService = app.injector.instanceOf[EmailService]

  "EmailService" - {

    val fakeRequest = FakeRequest("GET", "/").withSessionId()
    val fakeRequestInWelsh = fakeRequest.withLangWelsh()

    "buildEmailRequest" - {

      "Should return an EmailRequest" - {

        "request in english" in {
          val expectedResult: EmailRequest = EmailRequest(
            to         = List(EmailAddress("joe_bloggs@gmail.com")),
            templateId = "payment_successful",
            parameters = EmailParameters(
              taxType          = "Self Assessment",
              taxReference     = "1234567895K",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = None,
              totalPaid        = Some("12.34")
            ),
            force      = false
          )

          val result = systemUnderTest.buildEmailRequest(
            TestJourneys.PfSa.testPfSaJourneySuccessDebit,
            emailAddress = EmailAddress("joe_bloggs@gmail.com"),
            isEnglish    = true
          )(fakeRequest.withEmailInSession(TestJourneys.PfSa.testPfSaJourneySuccessDebit._id, EmailAddress("joe_bloggs@gmail.com")))
          result shouldBe expectedResult
        }

        "request in welsh" in {
          val expectedResult: EmailRequest = EmailRequest(
            to         = List(EmailAddress("joe_bloggs@gmail.com")),
            templateId = "payment_successful_cy",
            parameters = EmailParameters(
              taxType          = "Hunanasesiad",
              taxReference     = "1234567895K",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = None,
              totalPaid        = Some("12.34")
            ),
            force      = false
          )

          val result = systemUnderTest.buildEmailRequest(
            TestJourneys.PfSa.testPfSaJourneySuccessDebit,
            emailAddress = EmailAddress("joe_bloggs@gmail.com"),
            isEnglish    = false
          )(fakeRequestInWelsh.withEmailInSession(TestJourneys.PfSa.testPfSaJourneySuccessDebit._id, EmailAddress("joe_bloggs@gmail.com")))
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

            val result = systemUnderTest.buildEmailParameters(TestJourneys.PfSa.testPfSaJourneySuccessDebit)(fakeRequest)
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

            val result = systemUnderTest.buildEmailParameters(TestJourneys.PfSa.testPfSaJourneySuccessCredit)(fakeRequest)
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

            val result = systemUnderTest.buildEmailParameters(TestJourneys.PfSa.testPfSaJourneySuccessDebit)(fakeRequestInWelsh)
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

            val result = systemUnderTest.buildEmailParameters(TestJourneys.PfSa.testPfSaJourneySuccessCredit)(fakeRequestInWelsh)
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

            val result = systemUnderTest.buildEmailParameters(TestJourneys.BtaSa.testBtaSaJourneySuccessDebit)(fakeRequest)
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

            val result = systemUnderTest.buildEmailParameters(TestJourneys.BtaSa.testBtaSaJourneySuccessCredit)(fakeRequest)
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

            val result = systemUnderTest.buildEmailParameters(TestJourneys.BtaSa.testBtaSaJourneySuccessDebit)(fakeRequestInWelsh)
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

            val result = systemUnderTest.buildEmailParameters(TestJourneys.BtaSa.testBtaSaJourneySuccessCredit)(fakeRequestInWelsh)
            result shouldBe expectedResult
          }
        }

        "when origin is PtaSa" - {
          "debit in english" in {
            //todo: Mike
          }
          "credit in english" in {
            //todo: Mike
          }
          "debit in welsh" in {
            //todo: Mike
          }
          "credit in welsh" in {
            //todo: Mike
          }
        }

        "when origin is ItSa" - {
          "debit in english" in {
            //todo: Mike
          }
          "credit in english" in {
            //todo: Mike
          }
          "debit in welsh" in {
            //todo: Mike
          }
          "credit in welsh" in {
            //todo: Mike
          }
        }
      }

    }

    "sendEmail" - {
      "should send an email successfully" in {
        //todo: Mike, add wiremock assertion
        val result = systemUnderTest.sendEmail(
          journey      = TestJourneys.PfSa.testPfSaJourneySuccessCredit,
          emailAddress = EmailAddress("blah@blah.com"),
          isEnglish    = true
        )(HeaderCarrier(), fakeRequest)
        whenReady(result)(_ => succeed)
        //verify request was sent
      }
    }

  }

}
