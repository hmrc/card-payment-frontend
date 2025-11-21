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

import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.time.{Milliseconds, Span}
import payapi.cardpaymentjourney.model.journey.{Journey, JourneySpecificData, JsdPfSa}
import play.api.i18n.MessagesApi
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{AnyContent, AnyContentAsEmpty}
import play.api.test.FakeRequest
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.Languages.English
import uk.gov.hmrc.cardpaymentfrontend.models.cardpayment._
import uk.gov.hmrc.cardpaymentfrontend.models.payapirequest.{FailWebPaymentRequest, SucceedWebPaymentRequest}
import uk.gov.hmrc.cardpaymentfrontend.models.{Address, EmailAddress}
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec
import uk.gov.hmrc.cardpaymentfrontend.testsupport.TestOps.FakeRequestOps
import uk.gov.hmrc.cardpaymentfrontend.testsupport.stubs.{AuditConnectorStub, CardPaymentStub, CdsStub, EmailStub, ModsStub, PassengersStub, PayApiStub}
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.{TestJourneys, TestPayApiData}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDateTime
import java.util.Base64

class CardPaymentServiceSpec extends ItSpec {

  override protected lazy val configOverrides: Map[String, Any] = Map[String, Any](
    "auditing.enabled" -> true
  )

  val systemUnderTest: CardPaymentService = app.injector.instanceOf[CardPaymentService]
  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  implicit val headerCarrier: HeaderCarrier = HeaderCarrier()
  implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  def fakeJourneyRequest(journey: Journey[JourneySpecificData], withEmail: Boolean): JourneyRequest[AnyContent] = {
    if (withEmail) new JourneyRequest(journey, FakeRequest().withEmailAndAddressInSession(journey._id))
    else new JourneyRequest(journey, FakeRequest().withAddressInSession(journey._id))
  }

  val testJourneyBeforeBeginWebPayment: Journey[JsdPfSa] = TestJourneys.PfSa.journeyBeforeBeginWebPayment
  val testJourneyAfterBeginWebPayment: Journey[JsdPfSa] = TestJourneys.PfSa.journeyAfterBeginWebPayment
  val testJourneyAfterSucceedDebitWebPayment: Journey[JsdPfSa] = TestJourneys.PfSa.journeyAfterSucceedDebitWebPayment
  val testAddress: Address = Address("made up street", postcode = Some("AA11AA"), country = "GBR")
  val testEmail: EmailAddress = EmailAddress("some@email.com")

  "CardPaymentService" - {

    "returnToHmrcUrl" - {

      "should return a url that matches barclaycards regex for urlAddress, with journeyId encrypted and base64 encoded" in {
        val testJourney = TestJourneys.PfSa.journeyBeforeBeginWebPayment
        val returnToHmrcUrl = systemUnderTest.returnToHmrcUrl(testJourney._id)

        returnToHmrcUrl matches """^[A-Za-z0-9_ "!@#$&',*+/=()^.:-]{1,1024}$""" shouldBe true
      }

      "should return the correct journey, but encrypted and then base64 encoded" in {
        val cryptoService = app.injector.instanceOf[CryptoService]
        val testJourney = TestJourneys.PfSa.journeyBeforeBeginWebPayment
        val returnToHmrcUrl = systemUnderTest.returnToHmrcUrl(testJourney._id)
        val journeyIdBase64Encoded = returnToHmrcUrl.split("/return-to-hmrc/").lastOption
        val decodedJourneyId = journeyIdBase64Encoded.map(s => new String(Base64.getDecoder.decode(s.getBytes)))
        val decryptedJourneyId = decodedJourneyId.map(cryptoService.decryptString)

        decryptedJourneyId shouldBe Some(testJourney._id.value)
      }

    }

    "initiatePayment" - {

      implicit val journeyRequest: JourneyRequest[_] = fakeJourneyRequest(journey   = testJourneyBeforeBeginWebPayment, withEmail = true)

      val expectedCardPaymentInitiatePaymentResponse = CardPaymentInitiatePaymentResponse("someiframeurl", "sometransactionref")

      "should return a CardPaymentInitiatePaymentResponse when card-payment backend returns one" in {
        PayApiStub.stubForUpdateBeginWebPayment2xx(testJourneyBeforeBeginWebPayment._id)
        CardPaymentStub.InitiatePayment.stubForInitiatePayment2xx(expectedCardPaymentInitiatePaymentResponse)
        val result = systemUnderTest.initiatePayment(testJourneyBeforeBeginWebPayment, testAddress, Some(testEmail), English).futureValue
        result shouldBe expectedCardPaymentInitiatePaymentResponse
      }

      "should update pay-api journey with BeginWebPaymentRequest when call to card-payment backend succeeds" in {
        PayApiStub.stubForUpdateBeginWebPayment2xx(testJourneyBeforeBeginWebPayment._id)
        CardPaymentStub.InitiatePayment.stubForInitiatePayment2xx(expectedCardPaymentInitiatePaymentResponse)
        systemUnderTest.initiatePayment(testJourneyBeforeBeginWebPayment, testAddress, Some(testEmail), English).futureValue
        PayApiStub.verifyUpdateBeginWebPayment(1, testJourneyBeforeBeginWebPayment._id)
      }

      "should trigger an explicit paymentAttempt audit event" in {
        PayApiStub.stubForUpdateBeginWebPayment2xx(testJourneyBeforeBeginWebPayment._id)
        CardPaymentStub.InitiatePayment.stubForInitiatePayment2xx(expectedCardPaymentInitiatePaymentResponse)
        systemUnderTest.initiatePayment(testJourneyBeforeBeginWebPayment, testAddress, Some(testEmail), English).futureValue
        PayApiStub.verifyUpdateBeginWebPayment(1, testJourneyBeforeBeginWebPayment._id)
        AuditConnectorStub.verifyEventAudited(
          auditType  = "PaymentAttempt",
          auditEvent = Json.parse(
            """
              |{
              | "address": {
              |   "line1": "made up street",
              |   "postcode": "AA11AA",
              |   "country": "GBR"
              | },
              | "emailAddress": "blah@blah.com",
              | "loggedIn": false,
              | "merchantCode": "SAEE",
              | "paymentOrigin": "PfSa",
              | "paymentReference": "1234567895K",
              | "paymentTaxType": "selfAssessment",
              | "paymentTotal": 12.34,
              | "transactionReference": "sometransactionref"
              |}""".stripMargin
          ).as[JsObject]
        )
      }
    }

    "finishPayment" - {
      val testTime = LocalDateTime.now(FrozenTime.clock)
      val testCardPaymentResult = CardPaymentResult(CardPaymentFinishPaymentResponses.Successful, AdditionalPaymentInfo(Some("debit"), Some(123), Some(testTime)))

      "should return Some[CardPaymentResult] when one is returned from card-payment backend" in {
        PayApiStub.stubForUpdateSucceedWebPayment2xx(testJourneyBeforeBeginWebPayment._id)
        CardPaymentStub.AuthAndCapture.stubForAuthAndCapture2xx("sometransactionref", testCardPaymentResult)
        val result = systemUnderTest.finishPayment("sometransactionref", testJourneyAfterBeginWebPayment._id.value, English)(fakeJourneyRequest(testJourneyAfterBeginWebPayment, withEmail = false), messagesApi).futureValue
        result shouldBe Some(CardPaymentResult(CardPaymentFinishPaymentResponses.Successful, AdditionalPaymentInfo(Some("debit"), Some(123), Some(testTime))))
      }

      "should update pay-api with SucceedWebPaymentRequest when call to card-payment backend succeeds" in {
        PayApiStub.stubForUpdateSucceedWebPayment2xx(testJourneyBeforeBeginWebPayment._id)
        CardPaymentStub.AuthAndCapture.stubForAuthAndCapture2xx("sometransactionref", testCardPaymentResult)
        systemUnderTest.finishPayment("sometransactionref", testJourneyAfterBeginWebPayment._id.value, English)(fakeJourneyRequest(testJourneyAfterBeginWebPayment, withEmail = false), messagesApi).futureValue
        PayApiStub.verifyUpdateSucceedWebPayment(1, testJourneyAfterBeginWebPayment._id, testTime)
      }

      "should update pay-api with FailWebPaymentRequest when call to card-payment backend indicates failure" in {
        val testCardPaymentResult = CardPaymentResult(CardPaymentFinishPaymentResponses.Failed, AdditionalPaymentInfo(Some("debit"), None, Some(testTime)))
        CardPaymentStub.AuthAndCapture.stubForAuthAndCapture2xx("sometransactionref", testCardPaymentResult)
        systemUnderTest.finishPayment("sometransactionref", testJourneyAfterBeginWebPayment._id.value, English)(fakeJourneyRequest(testJourneyAfterBeginWebPayment, withEmail = false), messagesApi).futureValue
        PayApiStub.verifyUpdateFailWebPayment(1, testJourneyAfterBeginWebPayment._id, testTime)
      }

      "should update pay-api with CancelWebPaymentRequest when call to card-payment backend indicates Cancelled" in {
        val testCardPaymentResult = CardPaymentResult(CardPaymentFinishPaymentResponses.Cancelled, AdditionalPaymentInfo(None, None, None))
        CardPaymentStub.AuthAndCapture.stubForAuthAndCapture2xx("sometransactionref", testCardPaymentResult)
        systemUnderTest.finishPayment("sometransactionref", testJourneyAfterBeginWebPayment._id.value, English)(fakeJourneyRequest(testJourneyAfterBeginWebPayment, withEmail = false), messagesApi).futureValue
        PayApiStub.verifyUpdateCancelWebPayment(1, testJourneyAfterBeginWebPayment._id)
      }

      "should send an email when there is one in session, aswell as update pay-api when journey is in sent state" in {
        PayApiStub.stubForUpdateSucceedWebPayment2xx(testJourneyAfterBeginWebPayment._id)
        PayApiStub.stubForFindByJourneyId2xx(testJourneyAfterSucceedDebitWebPayment._id)(testJourneyAfterSucceedDebitWebPayment)
        EmailStub.stubForSimpleSendEmail()
        CardPaymentStub.AuthAndCapture.stubForAuthAndCapture2xx("sometransactionref", testCardPaymentResult)
        systemUnderTest.finishPayment("sometransactionref", testJourneyAfterBeginWebPayment._id.value, English)(fakeJourneyRequest(testJourneyAfterBeginWebPayment, withEmail = true), messagesApi).futureValue
        PayApiStub.verifyUpdateSucceedWebPayment(1, testJourneyAfterBeginWebPayment._id, testTime)
        eventually(Timeout(Span(500, Milliseconds))) { EmailStub.verifySomeEmailWasSent() }
      }

      "should not send an email when there isn't one in session" in {
        PayApiStub.stubForUpdateSucceedWebPayment2xx(testJourneyAfterBeginWebPayment._id)
        CardPaymentStub.AuthAndCapture.stubForAuthAndCapture2xx("sometransactionref", testCardPaymentResult)
        systemUnderTest.finishPayment("sometransactionref", testJourneyAfterBeginWebPayment._id.value, English)(fakeJourneyRequest(testJourneyAfterBeginWebPayment, withEmail = false), messagesApi).futureValue
        PayApiStub.verifyUpdateSucceedWebPayment(1, testJourneyAfterBeginWebPayment._id, testTime)
        EmailStub.verifyEmailWasNotSent()
      }

      "should trigger an explicit paymentStatus audit event" in {
        CardPaymentStub.AuthAndCapture.stubForAuthAndCapture2xx("sometransactionref", testCardPaymentResult)

        systemUnderTest.finishPayment(
          "sometransactionref", testJourneyAfterBeginWebPayment._id.value, English
        )(
            fakeJourneyRequest(testJourneyAfterBeginWebPayment, withEmail = true), messagesApi
          )
          .futureValue

        PayApiStub.verifyUpdateSucceedWebPayment(1, testJourneyAfterBeginWebPayment._id, testTime)
        AuditConnectorStub.verifyEventAudited(
          auditType  = "PaymentResult",
          auditEvent = Json.parse(
            """
              |{
              | "address": {
              |  "line1" : "line1",
              |  "postcode" : "AA0AA0",
              |  "country" : "GBR"
              | },
              | "emailAddress": "blah@blah.com",
              | "loggedIn": false,
              | "merchantCode": "SAEE",
              | "paymentOrigin": "PfSa",
              | "paymentStatus" : "Successful",
              | "paymentReference": "1234567895K",
              | "paymentTaxType": "selfAssessment",
              | "paymentTotal": 12.34,
              | "transactionReference": "sometransactionref"
              |}""".stripMargin
          ).as[JsObject]
        )
      }

      "should trigger a notification to CDS when origin is PfCds and payment is successful" in {
        val testJourney = TestJourneys.PfCds.journeyAfterSucceedDebitWebPayment
        CardPaymentStub.AuthAndCapture.stubForAuthAndCapture2xx("sometransactionref", testCardPaymentResult)
        CdsStub.stubGetCashDepositSubscriptionDetail2xx(TestPayApiData.testCdsRef)
        CdsStub.stubSimpleNotification2xx()
        PayApiStub.stubForUpdateSucceedWebPayment2xx(testJourney._id)
        PayApiStub.stubForFindByJourneyId2xx(testJourney._id)(testJourney)
        systemUnderTest.finishPayment("sometransactionref", testJourney._id.value, English)(fakeJourneyRequest(testJourney, withEmail = false), messagesApi).futureValue
        eventually(Timeout(Span(500, Milliseconds))){ CdsStub.verifySimpleNotificationSent() }
      }

      "should not trigger a notification to CDS when origins is PfCds but payment was not successful" in {
        val testJourney = TestJourneys.PfCds.journeyAfterFailWebPayment
        PayApiStub.stubForUpdateFailWebPayment2xx(testJourney._id)
        PayApiStub.stubForFindByJourneyId2xx(testJourney._id)(testJourney)
        val testCardPaymentResult = CardPaymentResult(CardPaymentFinishPaymentResponses.Failed, AdditionalPaymentInfo(Some("debit"), None, Some(testTime)))
        CardPaymentStub.AuthAndCapture.stubForAuthAndCapture2xx("sometransactionref", testCardPaymentResult)
        systemUnderTest.finishPayment("sometransactionref", testJourney._id.value, English)(fakeJourneyRequest(testJourney, withEmail = false), messagesApi).futureValue
        eventually(Timeout(Span(500, Milliseconds))){ CdsStub.verifyNoNotificationSent() }
      }

      "should trigger a notification to Mods when origins is Mib and payment is successful" in {
        val testJourney = TestJourneys.Mib.journeyAfterSucceedDebitWebPayment
        CardPaymentStub.AuthAndCapture.stubForAuthAndCapture2xx("sometransactionref", testCardPaymentResult)
        ModsStub.stubNotification2xx()
        PayApiStub.stubForUpdateSucceedWebPayment2xx(testJourney._id)
        PayApiStub.stubForFindByJourneyId2xx(testJourney._id)(testJourney)
        systemUnderTest.finishPayment("sometransactionref", testJourney._id.value, English)(fakeJourneyRequest(testJourney, withEmail = false), messagesApi).futureValue
        eventually(Timeout(Span(500, Milliseconds))){ ModsStub.verifySimpleNotificationSent() }
      }

      "should not trigger a notification to Mods when origins is Mib but payment was not successful" in {
        val testJourney = TestJourneys.Mib.journeyAfterFailWebPayment
        val testCardPaymentResult = CardPaymentResult(CardPaymentFinishPaymentResponses.Failed, AdditionalPaymentInfo(None, None, None))
        CardPaymentStub.AuthAndCapture.stubForAuthAndCapture2xx("sometransactionref", testCardPaymentResult)
        PayApiStub.stubForUpdateSucceedWebPayment2xx(testJourney._id)
        PayApiStub.stubForFindByJourneyId2xx(testJourney._id)(testJourney)
        systemUnderTest.finishPayment("sometransactionref", testJourney._id.value, English)(fakeJourneyRequest(testJourney, withEmail = false), messagesApi).futureValue
        eventually(Timeout(Span(500, Milliseconds))){ ModsStub.verifyNoNotificationSent() }
      }

      "should not send an email for Mib origin as the email is handled by Mods service" in {
        val testJourney = TestJourneys.Mib.journeyAfterSucceedDebitWebPayment
        CardPaymentStub.AuthAndCapture.stubForAuthAndCapture2xx("sometransactionref", testCardPaymentResult)
        systemUnderTest.finishPayment("sometransactionref", testJourney._id.value, English)(fakeJourneyRequest(testJourney, withEmail = false), messagesApi).futureValue
        eventually(Timeout(Span(500, Milliseconds))){ EmailStub.verifyEmailWasNotSent() }
      }

      "should trigger a notification to passngers when origins is BcPngr and payment is successful" in {
        val testJourney = TestJourneys.BcPngr.journeyAfterSucceedDebitWebPayment
        CardPaymentStub.AuthAndCapture.stubForAuthAndCapture2xx("sometransactionref", testCardPaymentResult)
        PassengersStub.stubNotification2xx()
        PayApiStub.stubForUpdateSucceedWebPayment2xx(testJourney._id)
        PayApiStub.stubForFindByJourneyId2xx(testJourney._id)(testJourney)
        systemUnderTest.finishPayment("sometransactionref", testJourney._id.value, English)(fakeJourneyRequest(testJourney, withEmail = false), messagesApi).futureValue
        eventually(Timeout(Span(500, Milliseconds))){ PassengersStub.verifySimpleNotificationSent() }
      }

      "should not send an email for BcPngr origin as the email is handled by passengers service" in {
        val testJourney = TestJourneys.BcPngr.journeyAfterSucceedDebitWebPayment
        CardPaymentStub.AuthAndCapture.stubForAuthAndCapture2xx("sometransactionref", testCardPaymentResult)
        PassengersStub.stubNotification2xx()
        PayApiStub.stubForUpdateSucceedWebPayment2xx(testJourney._id)
        PayApiStub.stubForFindByJourneyId2xx(testJourney._id)(testJourney)
        systemUnderTest.finishPayment("sometransactionref", testJourney._id.value, English)(fakeJourneyRequest(testJourney, withEmail = false), messagesApi).futureValue
        eventually(Timeout(Span(500, Milliseconds))){ EmailStub.verifyEmailWasNotSent() }
      }

      "should trigger a notification to passengers when origins is BcPngr but payment failed" in {
        val testJourney = TestJourneys.BcPngr.journeyAfterFailWebPayment
        val testCardPaymentResult = CardPaymentResult(CardPaymentFinishPaymentResponses.Failed, AdditionalPaymentInfo(None, None, None))
        CardPaymentStub.AuthAndCapture.stubForAuthAndCapture2xx("sometransactionref", testCardPaymentResult)
        CardPaymentStub.CancelPayment.stubForCancelPayment2xx("sometransactionref", "PSEE")
        PayApiStub.stubForUpdateFailWebPayment2xx(testJourney._id)
        PayApiStub.stubForFindByJourneyId2xx(testJourney._id)(testJourney)
        PassengersStub.stubNotification2xx()
        systemUnderTest.finishPayment("sometransactionref", testJourney._id.value, English)(fakeJourneyRequest(testJourney, withEmail = false), messagesApi).futureValue
        eventually(Timeout(Span(1000, Milliseconds))){ PassengersStub.verifySimpleNotificationSent() }
      }
    }

    "cancelPayment" - {

      "should call the cancelPayment in the connector and the card-payment backend when given a valid journeyRequest" in {
        PayApiStub.stubForUpdateCancelWebPayment2xx(testJourneyAfterBeginWebPayment._id)
        CardPaymentStub.CancelPayment.stubForCancelPayment2xx("Some-transaction-ref", "SAEE")
        systemUnderTest.cancelPayment()(fakeJourneyRequest(testJourneyAfterBeginWebPayment, false)).futureValue
        CardPaymentStub.CancelPayment.verifyOne("Some-transaction-ref", "SAEE")
      }

      "should call the cancelPayment in the connector with welsh clientId when lang is welsh" in {
        PayApiStub.stubForUpdateCancelWebPayment2xx(testJourneyAfterBeginWebPayment._id)
        CardPaymentStub.CancelPayment.stubForCancelPayment2xx("Some-transaction-ref", "SAEC")
        systemUnderTest.cancelPayment()(new JourneyRequest(testJourneyAfterBeginWebPayment, FakeRequest().withLangWelsh())).futureValue
        CardPaymentStub.CancelPayment.verifyOne("Some-transaction-ref", "SAEC")
      }

      "should update pay api state with cancelled" in {
        systemUnderTest.cancelPayment()(fakeJourneyRequest(testJourneyAfterBeginWebPayment, false)).futureValue
        CardPaymentStub.CancelPayment.verifyOne("Some-transaction-ref", "SAEE")
        PayApiStub.verifyUpdateCancelWebPayment(1, testJourneyAfterBeginWebPayment._id)
      }
    }

    "cardPaymentResultIntoUpdateWebPaymentRequest" - {
      "return None when response inside CardPaymentResult is CardPaymentFinishPaymentResponses.Cancelled" in {
        val testCardPaymentResult = CardPaymentResult(CardPaymentFinishPaymentResponses.Cancelled, AdditionalPaymentInfo(None, None, None))
        val result = systemUnderTest.cardPaymentResultIntoUpdateWebPaymentRequest(testCardPaymentResult)
        result shouldBe None
      }

      "return Some[SucceedWebPaymentRequest] with payment info when response inside CardPaymentResult is CardPaymentFinishPaymentResponses.Successful " in {
        val testCardPaymentResult = CardPaymentResult(CardPaymentFinishPaymentResponses.Successful, AdditionalPaymentInfo(Some("cardcategory"), Some(123), Some(LocalDateTime.now(FrozenTime.clock))))
        val result = systemUnderTest.cardPaymentResultIntoUpdateWebPaymentRequest(testCardPaymentResult)
        result shouldBe Some(SucceedWebPaymentRequest("cardcategory", Some(123), LocalDateTime.parse("2059-11-25T16:33:51.880")))
      }

      "return Some[FailWebPaymentRequest] when response inside CardPaymentResult is CardPaymentFinishPaymentResponses.Failed" in {
        val testCardPaymentResult = CardPaymentResult(CardPaymentFinishPaymentResponses.Failed, AdditionalPaymentInfo(Some("cardcategory"), None, Some(LocalDateTime.now(FrozenTime.clock))))
        val result = systemUnderTest.cardPaymentResultIntoUpdateWebPaymentRequest(testCardPaymentResult)
        result shouldBe Some(FailWebPaymentRequest(LocalDateTime.parse("2059-11-25T16:33:51.880"), "cardcategory"))
      }
    }
  }
}
