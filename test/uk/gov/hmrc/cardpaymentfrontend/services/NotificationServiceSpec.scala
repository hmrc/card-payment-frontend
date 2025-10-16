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

import org.scalatest.concurrent.PatienceConfiguration.{Interval, Timeout}
import org.scalatest.time.{Milliseconds, Span}
import payapi.cardpaymentjourney.model.journey.{Journey, JsdBcPngr, JsdMib, JsdPfCds}
import payapi.corcommon.model.PaymentStatuses
import payapi.corcommon.model.mods.AmendmentReference
import payapi.corcommon.model.taxes.mib.MibReference
import play.api.libs.json.Json
import uk.gov.hmrc.cardpaymentfrontend.models.notifications._
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec
import uk.gov.hmrc.cardpaymentfrontend.testsupport.stubs.{CdsStub, ModsStub, PassengersStub}
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.TestJourneys
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDateTime

class NotificationServiceSpec extends ItSpec {

  private val systemUnderTest: NotificationService = app.injector.instanceOf[NotificationService]
  //sufficient amount of time for notifications to be fired.
  private val testTimeOut = Timeout(Span(400, Milliseconds))
  private val testInterval = Interval(Span(50, Milliseconds))

  "NotificationService" - {

    val testEventTime: LocalDateTime = LocalDateTime.parse("2059-11-25T16:33:51")
    val testDeclarationId: String = "16NLIWQ2W3AXAGWD52"

    "buildCdsNotification" - {

      "successfully build a CdsNotification" in {
        val testJourney = TestJourneys.PfCds.journeyAfterSucceedDebitWebPayment
        val result = systemUnderTest.buildCdsNotification(testJourney, testEventTime, testDeclarationId)
        result shouldBe CdsNotification(NotifyImmediatePaymentRequest(RequestCommon("2059-11-25T16:33:51Z", "Sometransactionref", "CDS", "OPS"), RequestDetail("CDSI191234567890", "12.34", "GBP", "16NLIWQ2W3AXAGWD52")))
      }
      "error when order in journey is missing" in {
        val testJourney = TestJourneys.PfCds.journeyAfterSucceedDebitWebPayment.copy(order = None)
        val error = intercept[Exception](systemUnderTest.buildCdsNotification(testJourney, testEventTime, testDeclarationId))
        error.getMessage shouldBe s"Expected defined order [${testJourney.toString}]"
      }
      "error when reference in journey is missing" in {
        val testJourney = TestJourneys.PfCds.journeyAfterSucceedDebitWebPayment.copy(journeySpecificData = TestJourneys.PfCds.journeyAfterSucceedDebitWebPayment.journeySpecificData.copy(cdsRef = None))
        val error = intercept[Exception](systemUnderTest.buildCdsNotification(testJourney, testEventTime, testDeclarationId))
        error.getMessage shouldBe s"Expected defined reference [${testJourney.toString}]"
      }
      "error when amountInPence in journey is missing" in {
        val testJourney = TestJourneys.PfCds.journeyAfterSucceedDebitWebPayment.copy(amountInPence = None)
        val error = intercept[Exception](systemUnderTest.buildCdsNotification(testJourney, testEventTime, testDeclarationId))
        error.getMessage shouldBe s"Expected defined amountInPence [${testJourney.toString}]"
      }
    }

    "sendCdsNotification" - {

      "should send a notification successfully to CDS" in {
        val testJourney: Journey[JsdPfCds] = TestJourneys.PfCds.journeyAfterSucceedDebitWebPayment
        CdsStub.stubGetCashDepositSubscriptionDetail2xx(testJourney.journeySpecificData.cdsRef.getOrElse(throw new RuntimeException("test data is wrong, should have cds ref")))
        CdsStub.stubNotification2xx(Json.toJson(
          systemUnderTest.buildCdsNotification(testJourney, testEventTime, testDeclarationId)
        ))

        systemUnderTest.sendCdsNotification(testJourney)(HeaderCarrier())

        eventually(timeout  = testTimeOut, interval = testInterval) {
          CdsStub.verifyGetCashDepositSubscriptionDetail(testJourney.journeySpecificData.cdsRef.getOrElse(throw new RuntimeException("test data is wrong, should have cds ref")))
          CdsStub.verifyNotificationSent(Json.parse(
            //language=JSON
            """{
              |  "notifyImmediatePaymentRequest" : {
              |    "requestCommon" : {
              |      "receiptDate" : "2059-11-25T16:33:51Z",
              |      "acknowledgementReference" : "Sometransactionref",
              |      "regime" : "CDS",
              |      "originatingSystem" : "OPS"
              |    },
              |    "requestDetail" : {
              |      "paymentReference" : "CDSI191234567890",
              |      "amountPaid" : "12.34",
              |      "unitType" : "GBP",
              |      "declarationID" : "16NLIWQ2W3AXAGWD52"
              |    }
              |  }
              |}""".stripMargin
          ))
        }

      }

      "should error when trying to obtain cdsSubscriptionDetails before sending notification if cdsRef is not present" in {
        val testJourney: Journey[JsdPfCds] = TestJourneys.PfCds.journeyAfterSucceedDebitWebPayment
        val testJourneyWithNoReference = testJourney.copy(journeySpecificData = TestJourneys.PfCds.journeyAfterSucceedDebitWebPayment.journeySpecificData.copy(cdsRef = None))

        val error = intercept[Exception](systemUnderTest.sendCdsNotification(testJourneyWithNoReference)(HeaderCarrier()))

        error.getMessage shouldBe "CDS reference missing for notification, this should never happen"

        eventually(timeout  = testTimeOut, interval = testInterval) {
          CdsStub.verifyNoneGetCashDepositSubscriptionDetail(testJourney.journeySpecificData.cdsRef.getOrElse(throw new RuntimeException("test data is wrong, should have cds ref")))
          CdsStub.verifyNoNotificationSent()
        }
      }

      "should call GetCashDepositSubscriptionDetail but not send notification if that call fails" in {
        val testJourney: Journey[JsdPfCds] = TestJourneys.PfCds.journeyAfterSucceedDebitWebPayment
        CdsStub.stubGetCashDepositSubscriptionDetail5xx(testJourney.journeySpecificData.cdsRef.getOrElse(throw new RuntimeException("test data is wrong, should have cds ref")))

        systemUnderTest.sendCdsNotification(testJourney)(HeaderCarrier())
        eventually(timeout  = testTimeOut, interval = testInterval) {
          CdsStub.verifyGetCashDepositSubscriptionDetail(testJourney.journeySpecificData.cdsRef.getOrElse(throw new RuntimeException("test data is wrong, should have cds ref")))
          CdsStub.verifyNoNotificationSent()
        }
      }
    }

    "buildModsNotification" - {
      "successfully build a ModsNotification" in {
        val testJourney = TestJourneys.Mib.journeyAfterSucceedDebitWebPayment
        val result = systemUnderTest.buildModsNotification(testJourney)
        result shouldBe ModsNotification(MibReference("MIBI1234567891"), Some(AmendmentReference(123456789)))
      }
    }

    "sendModsNotification" - {
      "should send a notification successfully to Mods" in {
        ModsStub.stubNotification2xx()
        val testJourney: Journey[JsdMib] = TestJourneys.Mib.journeyAfterSucceedDebitWebPayment
        systemUnderTest.sendModsNotification(testJourney)(HeaderCarrier())
        eventually(timeout  = testTimeOut, interval = testInterval) {
          ModsStub.verifyNotificationSent(Json.parse(
            //language=JSON
            """
              |{
              |   "chargeReference": "MIBI1234567891",
              |   "amendmentReference": 123456789
              |}
              |""".stripMargin
          ))
        }
      }
    }

    "buildPassengersNotification" - {

      "successfully build a PassengersNotification" in {
        val testJourney = TestJourneys.BcPngr.journeyAfterSucceedDebitWebPayment
        val result = systemUnderTest.buildPassengersNotification(testJourney, testEventTime)
        result shouldBe PassengersNotification(
          paymentId            = "TestJourneyId-44f9-ad7f-01e1d3d8f151",
          taxType              = "pngr",
          status               = PaymentStatuses.Successful,
          amountInPence        = 1234,
          commissionInPence    = 0,
          reference            = "XAPR9876543210",
          transactionReference = "Some-transaction-ref",
          notificationData     = Json.obj(),
          eventDateTime        = "2059-11-25T16:33:51"
        )
      }

      "error when order in journey is missing" in {
        val testJourney = TestJourneys.BcPngr.journeyAfterSucceedDebitWebPayment.copy(order = None)
        val error = intercept[Exception](systemUnderTest.buildPassengersNotification(testJourney, testEventTime))
        error.getMessage shouldBe s"Expected defined order [${testJourney.toString}]"
      }

      "error when amountInPence in journey is missing" in {
        val testJourney = TestJourneys.BcPngr.journeyAfterSucceedDebitWebPayment.copy(amountInPence = None)
        val error = intercept[Exception](systemUnderTest.buildPassengersNotification(testJourney, testEventTime))
        error.getMessage shouldBe s"Expected defined amountInPence [${testJourney.toString}]"
      }
    }

    "sendPassengersNotification" - {
      "should send a notification successfully to Passengers" in {
        PassengersStub.stubNotification2xx()
        val testJourney: Journey[JsdBcPngr] = TestJourneys.BcPngr.journeyAfterSucceedDebitWebPayment
        systemUnderTest.sendPassengersNotification(testJourney)(HeaderCarrier())
        eventually(timeout  = testTimeOut, interval = testInterval) {
          PassengersStub.verifyNotificationSent(Json.parse(
            //language=JSON
            """
              |{
              |  "paymentId" : "TestJourneyId-44f9-ad7f-01e1d3d8f151",
              |  "taxType" : "pngr",
              |  "status" : "Successful",
              |  "amountInPence" : 1234,
              |  "commissionInPence" : 0,
              |  "reference" : "XAPR9876543210",
              |  "transactionReference" : "Some-transaction-ref",
              |  "notificationData" : { },
              |  "eventDateTime" : "2059-11-25T16:33:51.88"
              |}""".stripMargin
          ))
        }
      }
    }

    "sendNotification" - {

      "should send a notification to CDS service for PfCds journey" in {
        val testJourney: Journey[JsdPfCds] = TestJourneys.PfCds.journeyAfterSucceedDebitWebPayment
        CdsStub.stubGetCashDepositSubscriptionDetail2xx(testJourney.journeySpecificData.cdsRef.getOrElse(throw new RuntimeException("test data is wrong, should have cds ref")))
        CdsStub.stubNotification2xx(Json.toJson(
          systemUnderTest.buildCdsNotification(testJourney, testEventTime, testDeclarationId)
        ))

        systemUnderTest.sendNotification(testJourney)(HeaderCarrier())

        eventually(timeout  = testTimeOut, interval = testInterval) {
          CdsStub.verifyGetCashDepositSubscriptionDetail(testJourney.journeySpecificData.cdsRef.getOrElse(throw new RuntimeException("test data is wrong, should have cds ref")))
          CdsStub.verifyNotificationSent(Json.parse(
            //language=JSON
            """{
              |  "notifyImmediatePaymentRequest" : {
              |    "requestCommon" : {
              |      "receiptDate" : "2059-11-25T16:33:51Z",
              |      "acknowledgementReference" : "Sometransactionref",
              |      "regime" : "CDS",
              |      "originatingSystem" : "OPS"
              |    },
              |    "requestDetail" : {
              |      "paymentReference" : "CDSI191234567890",
              |      "amountPaid" : "12.34",
              |      "unitType" : "GBP",
              |      "declarationID" : "16NLIWQ2W3AXAGWD52"
              |    }
              |  }
              |}""".stripMargin
          ))
        }
      }

      "should send a notification to passengers service for BcPngr journey" in {
        PassengersStub.stubNotification2xx()
        val testJourney: Journey[JsdBcPngr] = TestJourneys.BcPngr.journeyAfterSucceedDebitWebPayment
        val expectedNotificationJson = Json.parse(
          //language=JSON
          """
            |{
            |   "paymentId": "TestJourneyId-44f9-ad7f-01e1d3d8f151",
            |   "taxType": "pngr",
            |   "status": "Successful",
            |   "amountInPence": 1234,
            |   "commissionInPence": 0,
            |   "reference": "XAPR9876543210",
            |   "transactionReference": "Some-transaction-ref",
            |   "notificationData": {},
            |   "eventDateTime": "2059-11-25T16:33:51.88"
            |}
            |""".stripMargin
        )

        systemUnderTest.sendNotification(testJourney)(HeaderCarrier())

        eventually(timeout  = testTimeOut, interval = testInterval) {
          PassengersStub.verifyNotificationSent(expectedNotificationJson)
        }
      }

      "should send a notification to mods service for Mib journey" in {
        ModsStub.stubNotification2xx()
        val testJourney: Journey[JsdMib] = TestJourneys.Mib.journeyAfterSucceedDebitWebPayment
        val expectedNotificationJson = Json.parse(
          //language=JSON
          """
            |{
            |   "chargeReference": "MIBI1234567891",
            |   "amendmentReference": 123456789
            |}
            |""".stripMargin
        )

        systemUnderTest.sendNotification(testJourney)(HeaderCarrier())

        eventually(timeout  = testTimeOut, interval = testInterval) { ModsStub.verifyNotificationSent(expectedNotificationJson) }
      }

      "should not send a notification for an origin that isn't one of PfCds, Mib, BcPngr" in {
        systemUnderTest.sendNotification(TestJourneys.PfSa.journeyAfterSucceedDebitWebPayment)(HeaderCarrier())
        eventually(testTimeOut, testInterval) {
          CdsStub.verifyNoNotificationSent()
          ModsStub.verifyNoNotificationSent()
          PassengersStub.verifyNoNotificationSent()
        }
      }
    }
  }
}
