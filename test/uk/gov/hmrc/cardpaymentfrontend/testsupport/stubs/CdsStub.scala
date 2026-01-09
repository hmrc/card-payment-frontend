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

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalTo, equalToJson, get, getRequestedFor, post, postRequestedFor, stubFor, urlEqualTo, urlPathEqualTo, verify}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import payapi.corcommon.model.taxes.cds.CdsRef
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}

object CdsStub {

  private def getCashDepositSubscriptionDetailPath(cdsRef: CdsRef) = s"/accounts/getcashdepositsubscriptiondetails/v1?paymentReference=${cdsRef.value}"
  private val notificationPath: String = "/accounts/notifyimmediatepayment/v1"

  val responseJsonRefCheck: JsValue = Json.parse(
    s"""{"getCashDepositSubscriptionDetailsResponse":{"responseCommon":{"status":"Ok","processingDate":"2018-09-24T11:01:01Z"},"responseDetail":{"declarationID":"16NLIWQ2W3AXAGWD52","paymentReference":"CDSI191234567890","paymentReferenceDate":"2018-09-24T11:01:01Z","isPaymentReferenceActive":false,"paymentReferenceCancelDate":"2018-09-24T11:01:01Z"}}} """
  )

  def stubGetCashDepositSubscriptionDetail2xx(cdsRef: CdsRef): StubMapping =
    stubFor(
      get(urlEqualTo(getCashDepositSubscriptionDetailPath(cdsRef)))
        .withHeader("Authorization", equalTo(s"Bearer cdsAuthToken"))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withBody(Json.prettyPrint(responseJsonRefCheck))
        )
    )

  def stubGetCashDepositSubscriptionDetail5xx(cdsRef: CdsRef): StubMapping =
    stubFor(
      get(urlEqualTo(getCashDepositSubscriptionDetailPath(cdsRef)))
        .withHeader("Authorization", equalTo(s"Bearer cdsAuthToken"))
        .willReturn(
          aResponse()
            .withStatus(Status.SERVICE_UNAVAILABLE)
        )
    )

  def verifyGetCashDepositSubscriptionDetail(cdsRef: CdsRef, authToken: String = "cdsAuthToken"): Unit =
    verify(
      1,
      getRequestedFor(urlEqualTo(getCashDepositSubscriptionDetailPath(cdsRef))).withHeader("Authorization", equalTo(s"Bearer $authToken"))
    )

  def verifyNoneGetCashDepositSubscriptionDetail(cdsRef: CdsRef): Unit =
    verify(
      0,
      getRequestedFor(urlEqualTo(getCashDepositSubscriptionDetailPath(cdsRef)))
    )

  def stubNotification2xx(notificationJson: JsValue): StubMapping =
    stubFor(
      post(urlPathEqualTo(notificationPath))
        .withHeader("Authorization", equalTo(s"Bearer cdsAuthToken"))
        .withRequestBody(equalToJson(Json.prettyPrint(notificationJson)))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
        )
    )

  def stubSimpleNotification2xx(): StubMapping =
    stubFor(
      post(urlPathEqualTo(notificationPath))
        .withHeader("Authorization", equalTo(s"Bearer cdsAuthToken"))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
        )
    )

  def stubNotification5xx(): StubMapping =
    stubFor(
      post(urlPathEqualTo(notificationPath))
        .withHeader("Authorization", equalTo(s"Bearer cdsAuthToken"))
        .willReturn(
          aResponse()
            .withStatus(Status.SERVICE_UNAVAILABLE)
        )
    )

  def verifyNotificationSent(notificationJson: JsValue, authToken: String = "cdsAuthToken"): Unit =
    verify(
      1,
      postRequestedFor(urlEqualTo(notificationPath))
        .withHeader("Authorization", equalTo(s"Bearer $authToken"))
        .withRequestBody(equalToJson(Json.prettyPrint(notificationJson)))
    )

  def verifySimpleNotificationSent(): Unit =
    verify(1, postRequestedFor(urlEqualTo(notificationPath)))

  def verifyNoNotificationSent(): Unit =
    verify(0, postRequestedFor(urlEqualTo(notificationPath)))

}
