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

package uk.gov.hmrc.cardpaymentfrontend.connectors

import payapi.corcommon.model.AmountInPence
import uk.gov.hmrc.cardpaymentfrontend.models.cardpayment.{CardPaymentInitiatePaymentRequest, CardPaymentInitiatePaymentResponse}
import uk.gov.hmrc.cardpaymentfrontend.models.{Address, EmailAddress}
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec
import uk.gov.hmrc.cardpaymentfrontend.testsupport.stubs.CardPaymentStub
import uk.gov.hmrc.http.HeaderCarrier

class CardPaymentConnectorSpec extends ItSpec {

  val systemUnderTest: CardPaymentConnector = app.injector.instanceOf[CardPaymentConnector]
  implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  "CardPaymentConnector" - {

    "initiatePayment" - {
      "should return a CardPaymentInitiatePaymentResponse when card-payment backend returns valid json" in {
        val cardPaymentInitiatePaymentRequest = CardPaymentInitiatePaymentRequest("somereturnurl", "MIEE", "1234567895K", AmountInPence(1234), Address("teststreet", postCode    = "AA11AA", countryCode = "GBR"), Some(EmailAddress("test@email.com")))
        val expectedCardPaymentInitiatePaymentResponse = CardPaymentInitiatePaymentResponse("someiframeurl", "sometransactionref")

        CardPaymentStub.InitiatePayment.stubForInitiatePayment2xx(cardPaymentInitiatePaymentRequest, expectedCardPaymentInitiatePaymentResponse)

        val result = systemUnderTest.initiatePayment(cardPaymentInitiatePaymentRequest).futureValue
        result shouldBe expectedCardPaymentInitiatePaymentResponse
      }

      "should throw an exception when card-payment backend returns a 5xx server error" in {
        val cardPaymentInitiatePaymentRequest = CardPaymentInitiatePaymentRequest("somereturnurl", "MIEE", "1234567895", AmountInPence(123), Address("teststreet", postCode    = "AA11AA", countryCode = "GBR"), Some(EmailAddress("test@email.com")))
        CardPaymentStub.InitiatePayment.stubForInitiatePayment5xx()
        val error: Exception = intercept[Exception](systemUnderTest.initiatePayment(cardPaymentInitiatePaymentRequest).futureValue)
        error.getCause.getMessage should include(s"POST of 'http://localhost:${wireMockPort.toString}/card-payment/initiate-payment' returned 503.")
      }
    }

    // todo add some tests, currently we don't really care since it's just a HttpResponse... add this later
    "authAndSettle" is pending
  }

}
