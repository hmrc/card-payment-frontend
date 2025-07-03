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

    // needed for compiler. if you're adding a new extended origin, add the jsd to this type/list of types.
    type JsdBounds = JsdBtaSa with JsdAlcoholDuty with JsdPfAlcoholDuty with JsdPfEpayeP11d with JsdPfEpayeSeta with JsdPfEpayeLpp with JsdPfEpayeNi with JsdPtaSa with JsdBtaCt with JsdItSa with JsdPfCt with JsdPfSa with JsdPfEpayeLateCis with JsdPfVat with JsdBtaVat with JsdVcVatOther with JsdVcVatReturn with JsdPpt with JsdPfPpt with JsdBtaEpayeBill with JsdBtaEpayePenalty with JsdBtaEpayeGeneral with JsdBtaEpayeInterest with JsdBtaClass1aNi

    val scenarios: TableFor6[JourneyStatuses[_ >: JsdBounds <: JourneySpecificData], String, String, Option[String], Some[String], String] = Table(
      ("Journey", "Tax Type", "Tax Reference", "Commission", "Total Paid", "lang"),
      (PfSa, "Self Assessment", "ending with 7895K", None, Some("12.34"), "en"),
      (PfSa, "Self Assessment", "ending with 7895K", commission, Some("13.57"), "en"),
      (PfSa, "Hunanasesiad", "yn gorffen gyda 7895K", None, Some("12.34"), "cy"),
      (PfSa, "Hunanasesiad", "yn gorffen gyda 7895K", commission, Some("13.57"), "cy"),

      (BtaSa, "Self Assessment", "ending with 7895K", None, Some("12.34"), "en"),
      (BtaSa, "Self Assessment", "ending with 7895K", commission, Some("13.57"), "en"),
      (BtaSa, "Hunanasesiad", "yn gorffen gyda 7895K", None, Some("12.34"), "cy"),
      (BtaSa, "Hunanasesiad", "yn gorffen gyda 7895K", commission, Some("13.57"), "cy"),

      (PtaSa, "Self Assessment", "ending with 7895K", None, Some("12.34"), "en"),
      (PtaSa, "Self Assessment", "ending with 7895K", commission, Some("13.57"), "en"),
      (PtaSa, "Hunanasesiad", "yn gorffen gyda 7895K", None, Some("12.34"), "cy"),
      (PtaSa, "Hunanasesiad", "yn gorffen gyda 7895K", commission, Some("13.57"), "cy"),

      (ItSa, "Self Assessment", "ending with 7895K", None, Some("12.34"), "en"),
      (ItSa, "Self Assessment", "ending with 7895K", commission, Some("13.57"), "en"),
      (ItSa, "Hunanasesiad", "yn gorffen gyda 7895K", None, Some("12.34"), "cy"),
      (ItSa, "Hunanasesiad", "yn gorffen gyda 7895K", commission, Some("13.57"), "cy"),

      (AlcoholDuty, "Alcohol Duty", "ending with 56789", None, Some("12.34"), "en"),
      (AlcoholDuty, "Alcohol Duty", "ending with 56789", commission, Some("13.57"), "en"),
      (AlcoholDuty, "Toll Alcohol", "yn gorffen gyda 56789", None, Some("12.34"), "cy"),
      (AlcoholDuty, "Toll Alcohol", "yn gorffen gyda 56789", commission, Some("13.57"), "cy"),

      (PfAlcoholDuty, "Alcohol Duty", "ending with 56789", None, Some("12.34"), "en"),
      (PfAlcoholDuty, "Alcohol Duty", "ending with 56789", commission, Some("13.57"), "en"),
      (PfAlcoholDuty, "Toll Alcohol", "yn gorffen gyda 56789", None, Some("12.34"), "cy"),
      (PfAlcoholDuty, "Toll Alcohol", "yn gorffen gyda 56789", commission, Some("13.57"), "cy"),

      (BtaCt, "Corporation Tax", "ending with 0101A", None, Some("12.34"), "en"),
      (BtaCt, "Corporation Tax", "ending with 0101A", commission, Some("13.57"), "en"),
      (BtaCt, "Treth Gorfforaeth", "yn gorffen gyda 0101A", None, Some("12.34"), "cy"),
      (BtaCt, "Treth Gorfforaeth", "yn gorffen gyda 0101A", commission, Some("13.57"), "cy"),

      (PfCt, "Corporation Tax", "ending with 0101A", None, Some("12.34"), "en"),
      (PfCt, "Corporation Tax", "ending with 0101A", commission, Some("13.57"), "en"),
      (PfCt, "Treth Gorfforaeth", "yn gorffen gyda 0101A", None, Some("12.34"), "cy"),
      (PfCt, "Treth Gorfforaeth", "yn gorffen gyda 0101A", commission, Some("13.57"), "cy"),

      (PfEpayeNi, "Employers’ PAYE and National Insurance", "ending with 02503", None, Some("12.34"), "en"),
      (PfEpayeNi, "Employers’ PAYE and National Insurance", "ending with 02503", commission, Some("13.57"), "en"),
      (PfEpayeNi, "TWE ac Yswiriant Gwladol y Cyflogwr", "yn gorffen gyda 02503", None, Some("12.34"), "cy"),
      (PfEpayeNi, "TWE ac Yswiriant Gwladol y Cyflogwr", "yn gorffen gyda 02503", commission, Some("13.57"), "cy"),

      (PfEpayeLpp, "Employers’ PAYE late payment penalty", "ending with 89012", None, Some("12.34"), "en"),
      (PfEpayeLpp, "Employers’ PAYE late payment penalty", "ending with 89012", commission, Some("13.57"), "en"),
      (PfEpayeLpp, "Cosb y Cyflogwr am dalu TWE yn hwyr", "yn gorffen gyda 89012", None, Some("12.34"), "cy"),
      (PfEpayeLpp, "Cosb y Cyflogwr am dalu TWE yn hwyr", "yn gorffen gyda 89012", commission, Some("13.57"), "cy"),

      (PfEpayeSeta, "Employers’ PAYE Settlement Agreement", "ending with 89012", None, Some("12.34"), "en"),
      (PfEpayeSeta, "Employers’ PAYE Settlement Agreement", "ending with 89012", commission, Some("13.57"), "en"),
      (PfEpayeSeta, "Cytundeb Setliad TWE y Cyflogwr", "yn gorffen gyda 89012", None, Some("12.34"), "cy"),
      (PfEpayeSeta, "Cytundeb Setliad TWE y Cyflogwr", "yn gorffen gyda 89012", commission, Some("13.57"), "cy"),

      (PfEpayeLateCis, "Construction Industry Scheme (CIS) late filing penalty", "ending with 89012", None, Some("12.34"), "en"),
      (PfEpayeLateCis, "Construction Industry Scheme (CIS) late filing penalty", "ending with 89012", commission, Some("13.57"), "en"),
      (PfEpayeLateCis, "Cynllun y Diwydiant Adeiladu (CIS) - cosb am dalu’n hwyr", "yn gorffen gyda 89012", None, Some("12.34"), "cy"),
      (PfEpayeLateCis, "Cynllun y Diwydiant Adeiladu (CIS) - cosb am dalu’n hwyr", "yn gorffen gyda 89012", commission, Some("13.57"), "cy"),

      (PfEpayeP11d, "Employers’ Class 1A National Insurance", "ending with 02513", None, Some("12.34"), "en"),
      (PfEpayeP11d, "Employers’ Class 1A National Insurance", "ending with 02513", commission, Some("13.57"), "en"),
      (PfEpayeP11d, "Yswiriant Gwladol Dosbarth 1A y Cyflogwr", "yn gorffen gyda 02513", None, Some("12.34"), "cy"),
      (PfEpayeP11d, "Yswiriant Gwladol Dosbarth 1A y Cyflogwr", "yn gorffen gyda 02513", commission, Some("13.57"), "cy"),

      (PfVat, "Vat", "ending with 64805", None, Some("12.34"), "en"),
      (PfVat, "Vat", "ending with 64805", commission, Some("13.57"), "en"),
      (PfVat, "TAW", "yn gorffen gyda 64805", None, Some("12.34"), "cy"),
      (PfVat, "TAW", "yn gorffen gyda 64805", commission, Some("13.57"), "cy"),

      (BtaVat, "Vat", "ending with 64805", None, Some("12.34"), "en"),
      (BtaVat, "Vat", "ending with 64805", commission, Some("13.57"), "en"),
      (BtaVat, "TAW", "yn gorffen gyda 64805", None, Some("12.34"), "cy"),
      (BtaVat, "TAW", "yn gorffen gyda 64805", commission, Some("13.57"), "cy"),

      (VcVatOther, "Vat", "ending with 64805", None, Some("12.34"), "en"),
      (VcVatOther, "Vat", "ending with 64805", commission, Some("13.57"), "en"),
      (VcVatOther, "TAW", "yn gorffen gyda 64805", None, Some("12.34"), "cy"),
      (VcVatOther, "TAW", "yn gorffen gyda 64805", commission, Some("13.57"), "cy"),

      (VcVatReturn, "Vat", "ending with 64805", None, Some("12.34"), "en"),
      (VcVatReturn, "Vat", "ending with 64805", commission, Some("13.57"), "en"),
      (VcVatReturn, "TAW", "yn gorffen gyda 64805", None, Some("12.34"), "cy"),
      (VcVatReturn, "TAW", "yn gorffen gyda 64805", commission, Some("13.57"), "cy"),

      (Ppt, "Plastic Packaging Tax", "ending with 12345", None, Some("12.34"), "en"),
      (Ppt, "Plastic Packaging Tax", "ending with 12345", commission, Some("13.57"), "en"),
      (Ppt, "Dreth Deunydd Pacio Plastig", "yn gorffen gyda 12345", None, Some("12.34"), "cy"),
      (Ppt, "Dreth Deunydd Pacio Plastig", "yn gorffen gyda 12345", commission, Some("13.57"), "cy"),

      (PfPpt, "Plastic Packaging Tax", "ending with 12345", None, Some("12.34"), "en"),
      (PfPpt, "Plastic Packaging Tax", "ending with 12345", commission, Some("13.57"), "en"),
      (PfPpt, "Dreth Deunydd Pacio Plastig", "yn gorffen gyda 12345", None, Some("12.34"), "cy"),
      (PfPpt, "Dreth Deunydd Pacio Plastig", "yn gorffen gyda 12345", commission, Some("13.57"), "cy"),

      (BtaEpayeBill, "Employers’ PAYE and National Insurance", "ending with 02702", None, Some("12.34"), "en"),
      (BtaEpayeBill, "Employers’ PAYE and National Insurance", "ending with 02702", commission, Some("13.57"), "en"),
      (BtaEpayeBill, "TWE ac Yswiriant Gwladol y Cyflogwr", "yn gorffen gyda 02702", None, Some("12.34"), "cy"),
      (BtaEpayeBill, "TWE ac Yswiriant Gwladol y Cyflogwr", "yn gorffen gyda 02702", commission, Some("13.57"), "cy"),

      (BtaEpayePenalty, "Employers’ PAYE late payment or filing penalty", "ending with 78900", None, Some("12.34"), "en"),
      (BtaEpayePenalty, "Employers’ PAYE late payment or filing penalty", "ending with 78900", commission, Some("13.57"), "en"),
      (BtaEpayePenalty, "Cosb y Cyflogwr am dalu TWE yn hwyr", "yn gorffen gyda 78900", None, Some("12.34"), "cy"),
      (BtaEpayePenalty, "Cosb y Cyflogwr am dalu TWE yn hwyr", "yn gorffen gyda 78900", commission, Some("13.57"), "cy"),

      (BtaEpayeGeneral, "Employers’ PAYE and National Insurance", "ending with 02702", None, Some("12.34"), "en"),
      (BtaEpayeGeneral, "Employers’ PAYE and National Insurance", "ending with 02702", commission, Some("13.57"), "en"),
      (BtaEpayeGeneral, "TWE ac Yswiriant Gwladol y Cyflogwr", "yn gorffen gyda 02702", None, Some("12.34"), "cy"),
      (BtaEpayeGeneral, "TWE ac Yswiriant Gwladol y Cyflogwr", "yn gorffen gyda 02702", commission, Some("13.57"), "cy"),

      (BtaEpayeInterest, "Employers’ PAYE interest payment", "ending with 90123", None, Some("12.34"), "en"),
      (BtaEpayeInterest, "Employers’ PAYE interest payment", "ending with 90123", commission, Some("13.57"), "en"),
      (BtaEpayeInterest, "Taliad llog TWE cyflogwr", "yn gorffen gyda 90123", None, Some("12.34"), "cy"),
      (BtaEpayeInterest, "Taliad llog TWE cyflogwr", "yn gorffen gyda 90123", commission, Some("13.57"), "cy"),

      (BtaClass1aNi, "Employers’ Class 1A National Insurance", "ending with 02713", None, Some("12.34"), "en"),
      (BtaClass1aNi, "Employers’ Class 1A National Insurance", "ending with 02713", commission, Some("13.57"), "en"),
      (BtaClass1aNi, "Yswiriant Gwladol Dosbarth 1A y Cyflogwr", "yn gorffen gyda 02713", None, Some("12.34"), "cy"),
      (BtaClass1aNi, "Yswiriant Gwladol Dosbarth 1A y Cyflogwr", "yn gorffen gyda 02713", commission, Some("13.57"), "cy")

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

  "obfuscateReference" - {
    "should obfuscate the tax reference, appending a string and taking the right most characters" - {
      "in English" in {
        val result = systemUnderTest.obfuscateReference("123456789K")(fakeRequest)
        result shouldBe "ending with 6789K"
      }
      "in Welsh" in {
        val result = systemUnderTest.obfuscateReference("123456789K")(fakeRequestInWelsh)
        result shouldBe "yn gorffen gyda 6789K"
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
              taxReference     = "ending with 7895K",
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
              taxReference     = "yn gorffen gyda 7895K",
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
            "taxReference" : "ending with 7895K",
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
