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

import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.cardpaymentfrontend.models.EmailAddress
import uk.gov.hmrc.cardpaymentfrontend.models.email.{EmailParameters, EmailRequest}
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec
import uk.gov.hmrc.cardpaymentfrontend.testsupport.TestOps.FakeRequestOps
import uk.gov.hmrc.cardpaymentfrontend.testsupport.stubs.EmailStub
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
            TestJourneys.PfSa.journeyAfterSucceedDebitWebPayment,
            emailAddress = EmailAddress("joe_bloggs@gmail.com"),
            isEnglish    = true
          )(fakeRequest.withEmailInSession(TestJourneys.PfSa.journeyAfterSucceedDebitWebPayment._id, EmailAddress("joe_bloggs@gmail.com")))
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
            TestJourneys.PfSa.journeyAfterSucceedDebitWebPayment,
            emailAddress = EmailAddress("joe_bloggs@gmail.com"),
            isEnglish    = false
          )(fakeRequestInWelsh.withEmailInSession(TestJourneys.PfSa.journeyAfterSucceedDebitWebPayment._id, EmailAddress("joe_bloggs@gmail.com")))
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

            val result = systemUnderTest.buildEmailParameters(TestJourneys.PfSa.journeyAfterSucceedDebitWebPayment)(fakeRequest)
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

            val result = systemUnderTest.buildEmailParameters(TestJourneys.PfSa.journeyAfterSucceedCreditWebPayment)(fakeRequest)
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

            val result = systemUnderTest.buildEmailParameters(TestJourneys.PfSa.journeyAfterSucceedDebitWebPayment)(fakeRequestInWelsh)
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

            val result = systemUnderTest.buildEmailParameters(TestJourneys.PfSa.journeyAfterSucceedCreditWebPayment)(fakeRequestInWelsh)
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

            val result = systemUnderTest.buildEmailParameters(TestJourneys.BtaSa.journeyAfterSucceedDebitWebPayment)(fakeRequest)
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

            val result = systemUnderTest.buildEmailParameters(TestJourneys.BtaSa.journeyAfterSucceedCreditWebPayment)(fakeRequest)
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

            val result = systemUnderTest.buildEmailParameters(TestJourneys.BtaSa.journeyAfterSucceedDebitWebPayment)(fakeRequestInWelsh)
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

            val result = systemUnderTest.buildEmailParameters(TestJourneys.BtaSa.journeyAfterSucceedCreditWebPayment)(fakeRequestInWelsh)
            result shouldBe expectedResult
          }
        }

        "when origin is PtaSa" - {
          "debit in english" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "Self Assessment",
              taxReference     = "1234567895K",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = None,
              totalPaid        = Some("12.34")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.PtaSa.journeyAfterSucceedDebitWebPayment)(fakeRequest)
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

            val result = systemUnderTest.buildEmailParameters(TestJourneys.PtaSa.journeyAfterSucceedCreditWebPayment)(fakeRequest)
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

            val result = systemUnderTest.buildEmailParameters(TestJourneys.PtaSa.journeyAfterSucceedDebitWebPayment)(fakeRequestInWelsh)
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

            val result = systemUnderTest.buildEmailParameters(TestJourneys.PtaSa.journeyAfterSucceedCreditWebPayment)(fakeRequestInWelsh)
            result shouldBe expectedResult
          }
        }

        "when origin is ItSa" - {
          "debit in english" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "Self Assessment",
              taxReference     = "1234567895K",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = None,
              totalPaid        = Some("12.34")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.ItSa.journeyAfterSucceedDebitWebPayment)(fakeRequest)
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

            val result = systemUnderTest.buildEmailParameters(TestJourneys.ItSa.journeyAfterSucceedCreditWebPayment)(fakeRequest)
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

            val result = systemUnderTest.buildEmailParameters(TestJourneys.ItSa.journeyAfterSucceedDebitWebPayment)(fakeRequestInWelsh)
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

            val result = systemUnderTest.buildEmailParameters(TestJourneys.ItSa.journeyAfterSucceedCreditWebPayment)(fakeRequestInWelsh)
            result shouldBe expectedResult
          }
        }

        "when origin is AlcoholDuty" - {
          "debit in english" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "Alcohol Duty",
              taxReference     = "XMADP0123456789",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = None,
              totalPaid        = Some("12.34")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.AlcoholDuty.journeyAfterSucceedDebitWebPayment)(fakeRequest)
            result shouldBe expectedResult
          }
          "credit in english" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "Alcohol Duty",
              taxReference     = "XMADP0123456789",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = Some("1.23"),
              totalPaid        = Some("13.57")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.AlcoholDuty.journeyAfterSucceedCreditWebPayment)(fakeRequest)
            result shouldBe expectedResult
          }
          "debit in welsh" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "Toll Alcohol",
              taxReference     = "XMADP0123456789",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = None,
              totalPaid        = Some("12.34")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.AlcoholDuty.journeyAfterSucceedDebitWebPayment)(fakeRequestInWelsh)
            result shouldBe expectedResult
          }
          "credit in welsh" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "Toll Alcohol",
              taxReference     = "XMADP0123456789",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = Some("1.23"),
              totalPaid        = Some("13.57")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.AlcoholDuty.journeyAfterSucceedCreditWebPayment)(fakeRequestInWelsh)
            result shouldBe expectedResult
          }
        }

        "when origin is PfAlcoholDuty" - {
          "debit in english" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "Alcohol Duty",
              taxReference     = "XMADP0123456789",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = None,
              totalPaid        = Some("12.34")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.PfAlcoholDuty.journeyAfterSucceedDebitWebPayment)(fakeRequest)
            result shouldBe expectedResult
          }
          "credit in english" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "Alcohol Duty",
              taxReference     = "XMADP0123456789",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = Some("1.23"),
              totalPaid        = Some("13.57")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.PfAlcoholDuty.journeyAfterSucceedCreditWebPayment)(fakeRequest)
            result shouldBe expectedResult
          }
          "debit in welsh" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "Toll Alcohol",
              taxReference     = "XMADP0123456789",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = None,
              totalPaid        = Some("12.34")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.PfAlcoholDuty.journeyAfterSucceedDebitWebPayment)(fakeRequestInWelsh)
            result shouldBe expectedResult
          }
          "credit in welsh" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "Toll Alcohol",
              taxReference     = "XMADP0123456789",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = Some("1.23"),
              totalPaid        = Some("13.57")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.PfAlcoholDuty.journeyAfterSucceedCreditWebPayment)(fakeRequestInWelsh)
            result shouldBe expectedResult
          }
        }

        "when origin is BtaCt" - {
          "debit in english" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "Corporation Tax",
              taxReference     = "1097172564A00101A",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = None,
              totalPaid        = Some("12.34")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.BtaCt.journeyAfterSucceedDebitWebPayment)(fakeRequest)
            result shouldBe expectedResult
          }
          "credit in english" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "Corporation Tax",
              taxReference     = "1097172564A00101A",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = Some("1.23"),
              totalPaid        = Some("13.57")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.BtaCt.journeyAfterSucceedCreditWebPayment)(fakeRequest)
            result shouldBe expectedResult
          }
          "debit in welsh" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "Treth Gorfforaeth",
              taxReference     = "1097172564A00101A",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = None,
              totalPaid        = Some("12.34")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.BtaCt.journeyAfterSucceedDebitWebPayment)(fakeRequestInWelsh)
            result shouldBe expectedResult
          }
          "credit in welsh" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "Treth Gorfforaeth",
              taxReference     = "1097172564A00101A",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = Some("1.23"),
              totalPaid        = Some("13.57")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.BtaCt.journeyAfterSucceedCreditWebPayment)(fakeRequestInWelsh)
            result shouldBe expectedResult
          }
        }

        "when origin is PfCt" - {
          "debit in english" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "Corporation Tax",
              taxReference     = "1097172564A00101A",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = None,
              totalPaid        = Some("12.34")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.PfCt.journeyAfterSucceedDebitWebPayment)(fakeRequest)
            result shouldBe expectedResult
          }
          "credit in english" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "Corporation Tax",
              taxReference     = "1097172564A00101A",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = Some("1.23"),
              totalPaid        = Some("13.57")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.PfCt.journeyAfterSucceedCreditWebPayment)(fakeRequest)
            result shouldBe expectedResult
          }
          "debit in welsh" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "Treth Gorfforaeth",
              taxReference     = "1097172564A00101A",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = None,
              totalPaid        = Some("12.34")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.PfCt.journeyAfterSucceedDebitWebPayment)(fakeRequestInWelsh)
            result shouldBe expectedResult
          }
          "credit in welsh" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "Treth Gorfforaeth",
              taxReference     = "1097172564A00101A",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = Some("1.23"),
              totalPaid        = Some("13.57")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.PfCt.journeyAfterSucceedCreditWebPayment)(fakeRequestInWelsh)
            result shouldBe expectedResult
          }
        }

        "when origin is BtaEpayeBill" - {
          "debit in english" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "PAYE",
              taxReference     = "123PH456789002702",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = None,
              totalPaid        = Some("12.34")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.BtaEpayeBill.journeyAfterSucceedDebitWebPayment)(fakeRequest)
            result shouldBe expectedResult
          }
          "credit in english" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "PAYE",
              taxReference     = "123PH456789002702",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = Some("1.23"),
              totalPaid        = Some("13.57")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.BtaEpayeBill.journeyAfterSucceedCreditWebPayment)(fakeRequest)
            result shouldBe expectedResult
          }
          "debit in welsh" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "TAW",
              taxReference     = "123PH456789002702",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = None,
              totalPaid        = Some("12.34")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.BtaEpayeBill.journeyAfterSucceedDebitWebPayment)(fakeRequestInWelsh)
            result shouldBe expectedResult
          }
          "credit in welsh" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "TAW",
              taxReference     = "123PH456789002702",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = Some("1.23"),
              totalPaid        = Some("13.57")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.BtaEpayeBill.journeyAfterSucceedCreditWebPayment)(fakeRequestInWelsh)
            result shouldBe expectedResult
          }
        }

        "when origin is BtaEpayePenalty" - {
          "debit in english" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "PAYE",
              taxReference     = "123PH45678900",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = None,
              totalPaid        = Some("12.34")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.BtaEpayePenalty.journeyAfterSucceedDebitWebPayment)(fakeRequest)
            result shouldBe expectedResult
          }
          "credit in english" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "PAYE",
              taxReference     = "123PH45678900",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = Some("1.23"),
              totalPaid        = Some("13.57")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.BtaEpayePenalty.journeyAfterSucceedCreditWebPayment)(fakeRequest)
            result shouldBe expectedResult
          }
          "debit in welsh" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "TAW",
              taxReference     = "123PH45678900",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = None,
              totalPaid        = Some("12.34")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.BtaEpayePenalty.journeyAfterSucceedDebitWebPayment)(fakeRequestInWelsh)
            result shouldBe expectedResult
          }
          "credit in welsh" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "TAW",
              taxReference     = "123PH45678900",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = Some("1.23"),
              totalPaid        = Some("13.57")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.BtaEpayePenalty.journeyAfterSucceedCreditWebPayment)(fakeRequestInWelsh)
            result shouldBe expectedResult
          }
        }

        "when origin is BtaEpayeInterest" - {
          "debit in english" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "PAYE",
              taxReference     = "X1234567890123",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = None,
              totalPaid        = Some("12.34")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.BtaEpayeInterest.journeyAfterSucceedDebitWebPayment)(fakeRequest)
            result shouldBe expectedResult
          }
          "credit in english" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "PAYE",
              taxReference     = "X1234567890123",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = Some("1.23"),
              totalPaid        = Some("13.57")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.BtaEpayeInterest.journeyAfterSucceedCreditWebPayment)(fakeRequest)
            result shouldBe expectedResult
          }
          "debit in welsh" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "TAW",
              taxReference     = "X1234567890123",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = None,
              totalPaid        = Some("12.34")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.BtaEpayeInterest.journeyAfterSucceedDebitWebPayment)(fakeRequestInWelsh)
            result shouldBe expectedResult
          }
          "credit in welsh" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "TAW",
              taxReference     = "X1234567890123",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = Some("1.23"),
              totalPaid        = Some("13.57")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.BtaEpayeInterest.journeyAfterSucceedCreditWebPayment)(fakeRequestInWelsh)
            result shouldBe expectedResult
          }
        }

        "when origin is BtaEpayeGeneral" - {
          "debit in english" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "PAYE",
              taxReference     = "123PH456789002702",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = None,
              totalPaid        = Some("12.34")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.BtaEpayeGeneral.journeyAfterSucceedDebitWebPayment)(fakeRequest)
            result shouldBe expectedResult
          }
          "credit in english" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "PAYE",
              taxReference     = "123PH456789002702",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = Some("1.23"),
              totalPaid        = Some("13.57")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.BtaEpayeGeneral.journeyAfterSucceedCreditWebPayment)(fakeRequest)
            result shouldBe expectedResult
          }
          "debit in welsh" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "TAW",
              taxReference     = "123PH456789002702",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = None,
              totalPaid        = Some("12.34")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.BtaEpayeGeneral.journeyAfterSucceedDebitWebPayment)(fakeRequestInWelsh)
            result shouldBe expectedResult
          }
          "credit in welsh" in {
            val expectedResult: EmailParameters = EmailParameters(
              taxType          = "TAW",
              taxReference     = "123PH456789002702",
              paymentReference = "Some-transaction-ref",
              amountPaid       = "12.34",
              commission       = Some("1.23"),
              totalPaid        = Some("13.57")
            )

            val result = systemUnderTest.buildEmailParameters(TestJourneys.BtaEpayeGeneral.journeyAfterSucceedCreditWebPayment)(fakeRequestInWelsh)
            result shouldBe expectedResult
          }
        }

      }

    }

    "sendEmail" - {

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
            "commission" : "1.23",
            "totalPaid" : "13.57"
          },
          "force" : false
        }
        """
      )

      "should send an email successfully" in {
        val result = systemUnderTest.sendEmail(
          journey      = TestJourneys.PfSa.journeyAfterSucceedCreditWebPayment,
          emailAddress = EmailAddress("blah@blah.com"),
          isEnglish    = true
        )(HeaderCarrier(), fakeRequest)
        whenReady(result)(_ => succeed)
        EmailStub.verifyEmailWasSent(jsonBody)
      }
    }

  }

}
