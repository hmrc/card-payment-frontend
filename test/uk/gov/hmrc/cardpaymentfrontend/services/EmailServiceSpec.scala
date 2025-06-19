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

import org.scalatest.prop.{TableDrivenPropertyChecks, TableFor6}
import payapi.cardpaymentjourney.model.journey._
import play.api.i18n.{Lang, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.cardpaymentfrontend.models.EmailAddress
import uk.gov.hmrc.cardpaymentfrontend.models.email.{EmailParameters, EmailRequest}
import uk.gov.hmrc.cardpaymentfrontend.models.extendedorigins.ExtendedOrigin.OriginExtended
import uk.gov.hmrc.cardpaymentfrontend.testsupport.TestOps.FakeRequestOps
import uk.gov.hmrc.cardpaymentfrontend.testsupport.stubs.EmailStub
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.{JourneyStatuses, TestJourneys}
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.TestJourneys._
import uk.gov.hmrc.cardpaymentfrontend.testsupport.{ItSpec, TestHelpers}
import uk.gov.hmrc.http.HeaderCarrier

class EmailServiceSpec extends ItSpec with TableDrivenPropertyChecks {

  val systemUnderTest: EmailService = app.injector.instanceOf[EmailService]

  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/").withSessionId()
  val fakeRequestInWelsh: FakeRequest[AnyContentAsEmpty.type] = fakeRequest.withLangWelsh()

  "buildEmailParameters should return EmailParameters" - {
    val commission = Some("1.23")

    val scenarios: TableFor6[JourneyStatuses[_ >: JsdBtaSa with JsdAlcoholDuty with JsdPfAlcoholDuty with JsdPfEpayeP11d with JsdPfEpayeSeta with JsdPfEpayeLpp with JsdPfEpayeNi with JsdPtaSa with JsdBtaCt with JsdItSa with JsdPfCt with JsdPfSa with JsdPfEpayeLateCis <: JourneySpecificData], String, String, Option[String], Some[String], String] = Table(
      ("Journey", "Tax Type", "Tax Reference", "Commission", "Total Paid", "lang"),
      (PfSa, "Self Assessment", "1234567895K", None, Some("12.34"), "en"),
      (PfSa, "Self Assessment", "1234567895K", commission, Some("13.57"), "en"),
      (PfSa, "Hunanasesiad", "1234567895K", None, Some("12.34"), "cy"),
      (PfSa, "Hunanasesiad", "1234567895K", commission, Some("13.57"), "cy"),

      (BtaSa, "Self Assessment", "1234567895K", None, Some("12.34"), "en"),
      (BtaSa, "Self Assessment", "1234567895K", commission, Some("13.57"), "en"),
      (BtaSa, "Hunanasesiad", "1234567895K", None, Some("12.34"), "cy"),
      (BtaSa, "Hunanasesiad", "1234567895K", commission, Some("13.57"), "cy"),

      (PtaSa, "Self Assessment", "1234567895K", None, Some("12.34"), "en"),
      (PtaSa, "Self Assessment", "1234567895K", commission, Some("13.57"), "en"),
      (PtaSa, "Hunanasesiad", "1234567895K", None, Some("12.34"), "cy"),
      (PtaSa, "Hunanasesiad", "1234567895K", commission, Some("13.57"), "cy"),

      (ItSa, "Self Assessment", "1234567895K", None, Some("12.34"), "en"),
      (ItSa, "Self Assessment", "1234567895K", commission, Some("13.57"), "en"),
      (ItSa, "Hunanasesiad", "1234567895K", None, Some("12.34"), "cy"),
      (ItSa, "Hunanasesiad", "1234567895K", commission, Some("13.57"), "cy"),

      (AlcoholDuty, "Alcohol Duty", "XMADP0123456789", None, Some("12.34"), "en"),
      (AlcoholDuty, "Alcohol Duty", "XMADP0123456789", commission, Some("13.57"), "en"),
      (AlcoholDuty, "Toll Alcohol", "XMADP0123456789", None, Some("12.34"), "cy"),
      (AlcoholDuty, "Toll Alcohol", "XMADP0123456789", commission, Some("13.57"), "cy"),

      (PfAlcoholDuty, "Alcohol Duty", "XMADP0123456789", None, Some("12.34"), "en"),
      (PfAlcoholDuty, "Alcohol Duty", "XMADP0123456789", commission, Some("13.57"), "en"),
      (PfAlcoholDuty, "Toll Alcohol", "XMADP0123456789", None, Some("12.34"), "cy"),
      (PfAlcoholDuty, "Toll Alcohol", "XMADP0123456789", commission, Some("13.57"), "cy"),

      (BtaCt, "Corporation Tax", "1097172564A00101A", None, Some("12.34"), "en"),
      (BtaCt, "Corporation Tax", "1097172564A00101A", commission, Some("13.57"), "en"),
      (BtaCt, "Treth Gorfforaeth", "1097172564A00101A", None, Some("12.34"), "cy"),
      (BtaCt, "Treth Gorfforaeth", "1097172564A00101A", commission, Some("13.57"), "cy"),

      (PfCt, "Corporation Tax", "1097172564A00101A", None, Some("12.34"), "en"),
      (PfCt, "Corporation Tax", "1097172564A00101A", commission, Some("13.57"), "en"),
      (PfCt, "Treth Gorfforaeth", "1097172564A00101A", None, Some("12.34"), "cy"),
      (PfCt, "Treth Gorfforaeth", "1097172564A00101A", commission, Some("13.57"), "cy"),

      (PfEpayeNi, "Employers’ PAYE and National Insurance", "123PH456789002503", None, Some("12.34"), "en"),
      (PfEpayeNi, "Employers’ PAYE and National Insurance", "123PH456789002503", commission, Some("13.57"), "en"),
      (PfEpayeNi, "TWE ac Yswiriant Gwladol y Cyflogwr", "123PH456789002503", None, Some("12.34"), "cy"),
      (PfEpayeNi, "TWE ac Yswiriant Gwladol y Cyflogwr", "123PH456789002503", commission, Some("13.57"), "cy"),

      (PfEpayeLpp, "Employers’ PAYE late payment penalty", "XE123456789012", None, Some("12.34"), "en"),
      (PfEpayeLpp, "Employers’ PAYE late payment penalty", "XE123456789012", commission, Some("13.57"), "en"),
      (PfEpayeLpp, "Cosb y Cyflogwr am dalu TWE yn hwyr", "XE123456789012", None, Some("12.34"), "cy"),
      (PfEpayeLpp, "Cosb y Cyflogwr am dalu TWE yn hwyr", "XE123456789012", commission, Some("13.57"), "cy"),

      (PfEpayeSeta, "Employers’ PAYE Settlement Agreement", "XA123456789012", None, Some("12.34"), "en"),
      (PfEpayeSeta, "Employers’ PAYE Settlement Agreement", "XA123456789012", commission, Some("13.57"), "en"),
      (PfEpayeSeta, "Cytundeb Setliad TWE y Cyflogwr", "XA123456789012", None, Some("12.34"), "cy"),
      (PfEpayeSeta, "Cytundeb Setliad TWE y Cyflogwr", "XA123456789012", commission, Some("13.57"), "cy"),

      (PfEpayeLateCis, "Construction Industry Scheme (CIS) late filing penalty", "XE123456789012", None, Some("12.34"), "en"),
      (PfEpayeLateCis, "Construction Industry Scheme (CIS) late filing penalty", "XE123456789012", commission, Some("13.57"), "en"),
      (PfEpayeLateCis, "Cynllun y Diwydiant Adeiladu (CIS) - cosb am dalu’n hwyr", "XE123456789012", None, Some("12.34"), "cy"),
      (PfEpayeLateCis, "Cynllun y Diwydiant Adeiladu (CIS) - cosb am dalu’n hwyr", "XE123456789012", commission, Some("13.57"), "cy"),

      (PfEpayeP11d, "Employers’ Class 1A National Insurance", "123PH456789002513", None, Some("12.34"), "en"),
      (PfEpayeP11d, "Employers’ Class 1A National Insurance", "123PH456789002513", commission, Some("13.57"), "en"),
      (PfEpayeP11d, "Yswiriant Gwladol Dosbarth 1A y Cyflogwr", "123PH456789002513", None, Some("12.34"), "cy"),
      (PfEpayeP11d, "Yswiriant Gwladol Dosbarth 1A y Cyflogwr", "123PH456789002513", commission, Some("13.57"), "cy")

    )

    forAll(scenarios) { (j, taxType, taxReference, commission, totalPaid, lang) =>
      val cardType = if (commission.isDefined) "credit" else "debit"
      val origin = j.journeyBeforeBeginWebPayment.origin
      val request: FakeRequest[AnyContentAsEmpty.type] = if (lang == "en") fakeRequest else fakeRequestInWelsh
      val journey: Journey[_ >: JsdBtaSa with JsdAlcoholDuty with JsdPfAlcoholDuty with JsdPfEpayeP11d with JsdPfEpayeSeta with JsdPfEpayeLpp with JsdPfEpayeNi with JsdPtaSa with JsdBtaCt with JsdItSa with JsdPfCt with JsdPfSa with JsdPfEpayeLateCis <: JourneySpecificData] = if (cardType == "credit") j.journeyAfterSucceedCreditWebPayment else j.journeyAfterSucceedDebitWebPayment

      s"when origin is ${origin.entryName}, card type is $cardType in $lang" in {
        val expectedResult: EmailParameters = EmailParameters(
          taxType          = taxType,
          taxReference     = taxReference,
          paymentReference = "Some-transaction-ref",
          amountPaid       = "12.34",
          commission       = commission,
          totalPaid        = totalPaid
        )

        systemUnderTest.buildEmailParameters(journey)(request) shouldBe expectedResult
      }
    }

    "should have a messages populated for emailTaxTypeMessageKey for all origins" in {
      val messages: MessagesApi = app.injector.instanceOf[MessagesApi]
      val implementedOrigins = TestHelpers.implementedOrigins
      implementedOrigins.foreach { origin =>
        val msgKey = origin.lift.emailTaxTypeMessageKey

        msgKey.isEmpty shouldBe false
        //Check if the message is defined in either messages file, Doesn't matter which one
        //doesn't seem to care what the language is, if it exists in one of the language file, it'll return true
        messages.isDefinedAt(msgKey)(Lang("en")) shouldBe true
        //Check if the message is different in both languages, if they are the same the message is not in both files
        messages.preferred(Seq(Lang("en")))(msgKey) should not be messages.preferred(Seq(Lang("cy")))(msgKey)
      }
    }
  }

  "EmailService" - {
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
