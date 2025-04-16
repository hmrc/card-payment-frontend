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

package uk.gov.hmrc.cardpaymentfrontend.testsupport.stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status
import play.api.libs.json.Json
import uk.gov.hmrc.cardpaymentfrontend.models.cardpayment.{CardPaymentInitiatePaymentRequest, CardPaymentInitiatePaymentResponse, CardPaymentResult}

object CardPaymentStub {

  object InitiatePayment {
    private val path: String = "/card-payment/initiate-payment"

    def stubForInitiatePayment2xx(
        cardPaymentInitiatePaymentRequest:  CardPaymentInitiatePaymentRequest,
        cardPaymentInitiatePaymentResponse: CardPaymentInitiatePaymentResponse
    ): StubMapping = stubFor(
      post(urlPathEqualTo(path))
        .withRequestBody(equalToJson(Json.prettyPrint(Json.toJson(cardPaymentInitiatePaymentRequest))))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withBody(Json.prettyPrint(Json.toJson(cardPaymentInitiatePaymentResponse)))
        )
    )

    def stubForInitiatePayment5xx(): StubMapping = stubFor(
      post(urlPathEqualTo(path)).willReturn(
        aResponse()
          .withStatus(Status.SERVICE_UNAVAILABLE)
      )
    )
  }

  object AuthAndCapture {
    private def path(transactionReference: String): String = s"/card-payment/auth-and-settle/$transactionReference"
    def stubForAuthAndCapture2xx(
        transactionReference: String,
        cardPaymentResult:    CardPaymentResult
    ): StubMapping = stubFor(
      post(urlPathEqualTo(path(transactionReference)))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withBody(Json.prettyPrint(Json.toJson(cardPaymentResult)))
        )
    )
  }

}
