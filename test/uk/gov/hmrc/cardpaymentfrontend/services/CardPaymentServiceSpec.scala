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

import payapi.cardpaymentjourney.model.journey.{Journey, JsdPfSa}
import payapi.corcommon.model.AmountInPence
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.cardpaymentfrontend.models.Languages.English
import uk.gov.hmrc.cardpaymentfrontend.models.cardpayment._
import uk.gov.hmrc.cardpaymentfrontend.models.payapirequest.{FailWebPaymentRequest, SucceedWebPaymentRequest}
import uk.gov.hmrc.cardpaymentfrontend.models.{Address, EmailAddress}
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec
import uk.gov.hmrc.cardpaymentfrontend.testsupport.stubs.{CardPaymentStub, PayApiStub}
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.TestJourneys
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class CardPaymentServiceSpec extends ItSpec {

  val systemUnderTest: CardPaymentService = app.injector.instanceOf[CardPaymentService]

  implicit val headerCarrier: HeaderCarrier = HeaderCarrier()
  implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  val testJourney: Journey[JsdPfSa] = TestJourneys.PfSa.journeyBeforeBeginWebPayment
  val testAddress: Address = Address("made up street", postcode = "AA11AA", country = "GBR")
  val testEmail: EmailAddress = EmailAddress("some@email.com")

  "CardPaymentService" - {
    "initiatePayment" - {
      val cardPaymentInitiatePaymentRequest = CardPaymentInitiatePaymentRequest(
        redirectUrl         = "http://localhost:10155/pay-by-card/return-to-hmrc",
        clientId            = "SAEE",
        purchaseDescription = "1234567895K",
        purchaseAmount      = AmountInPence(1234),
        billingAddress      = BarclaycardAddress(
          line1       = "made up street",
          postCode    = "AA11AA",
          countryCode = "GBR"
        ),
        emailAddress        = Some(EmailAddress("some@email.com")),
        transactionNumber   = "00001999999999"
      )
      val expectedCardPaymentInitiatePaymentResponse = CardPaymentInitiatePaymentResponse("someiframeurl", "sometransactionref")

      "should return a CardPaymentInitiatePaymentResponse when card-payment backend returns one" in {
        CardPaymentStub.InitiatePayment.stubForInitiatePayment2xx(cardPaymentInitiatePaymentRequest, expectedCardPaymentInitiatePaymentResponse)
        val result = systemUnderTest.initiatePayment(testJourney, testAddress, Some(testEmail), English).futureValue
        result shouldBe expectedCardPaymentInitiatePaymentResponse
      }

      "should update pay-api journey with BeginWebPaymentRequest when call to card-payment backend succeeds" in {
        CardPaymentStub.InitiatePayment.stubForInitiatePayment2xx(cardPaymentInitiatePaymentRequest, expectedCardPaymentInitiatePaymentResponse)
        systemUnderTest.initiatePayment(testJourney, testAddress, Some(testEmail), English).futureValue
        PayApiStub.verifyUpdateBeginWebPayment(1, testJourney._id.value)
      }
    }

    "finishPayment" - {
      "should return Some[CardPaymentResult] when one is returned from card-payment backend" in {
        val testTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
        val testCardPaymentResult = CardPaymentResult(CardPaymentFinishPaymentResponses.Successful, AdditionalPaymentInfo(Some("debit"), Some(123), Some(testTime)))
        CardPaymentStub.AuthAndCapture.stubForAuthAndCapture2xx("sometransactionref", testCardPaymentResult)
        val result = systemUnderTest.finishPayment("sometransactionref", testJourney._id.value).futureValue
        result shouldBe Some(CardPaymentResult(CardPaymentFinishPaymentResponses.Successful, AdditionalPaymentInfo(Some("debit"), Some(123), Some(testTime))))
      }

      "should update pay-api with SucceedWebPaymentRequest when call to card-payment backend succeeds" in {
        val testTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
        val testCardPaymentResult = CardPaymentResult(CardPaymentFinishPaymentResponses.Successful, AdditionalPaymentInfo(Some("debit"), Some(123), Some(testTime)))
        CardPaymentStub.AuthAndCapture.stubForAuthAndCapture2xx("sometransactionref", testCardPaymentResult)
        systemUnderTest.finishPayment("sometransactionref", testJourney._id.value).futureValue
        PayApiStub.verifyUpdateSucceedWebPayment(1, testJourney._id.value, testTime.toString)
      }

      "should update pay-api with FailWebPaymentRequest when call to card-payment backend indicates failure" in {
        val testTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
        val testCardPaymentResult = CardPaymentResult(CardPaymentFinishPaymentResponses.Failed, AdditionalPaymentInfo(Some("debit"), None, Some(testTime)))
        CardPaymentStub.AuthAndCapture.stubForAuthAndCapture2xx("sometransactionref", testCardPaymentResult)
        systemUnderTest.finishPayment("sometransactionref", testJourney._id.value).futureValue
        PayApiStub.verifyUpdateFailWebPayment(1, testJourney._id.value, testTime.toString)
      }

      "should update pay-api with CancelWebPaymentRequest when call to card-payment backend indicates Cancelled" in {
        val testCardPaymentResult = CardPaymentResult(CardPaymentFinishPaymentResponses.Cancelled, AdditionalPaymentInfo(None, None, None))
        CardPaymentStub.AuthAndCapture.stubForAuthAndCapture2xx("sometransactionref", testCardPaymentResult)
        systemUnderTest.finishPayment("sometransactionref", testJourney._id.value).futureValue
        PayApiStub.verifyUpdateCancelWebPayment(1, testJourney._id.value)
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
