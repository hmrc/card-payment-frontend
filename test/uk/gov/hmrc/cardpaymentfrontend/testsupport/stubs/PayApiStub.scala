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
import payapi.cardpaymentjourney.model.journey.{Journey, JourneySpecificData}
import payapi.corcommon.model.JourneyId
import play.api.http.Status
import play.api.libs.json.Json

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object PayApiStub {

  private val findLatestBySessionIdPath: String = s"/pay-api/journey/find-latest-by-session-id"

  def stubForFindBySessionId2xx(journey: Journey[JourneySpecificData]): StubMapping = stubFor(
    get(urlPathEqualTo(findLatestBySessionIdPath)).willReturn(
      aResponse()
        .withStatus(Status.OK)
        .withBody(Json.prettyPrint(Json.toJson(journey)))
    )
  )

  def stubForFindBySessionId404: StubMapping = stubFor(
    get(urlPathEqualTo(findLatestBySessionIdPath)).willReturn(
      aResponse()
        .withStatus(Status.NOT_FOUND)
    )
  )

  def stubForFindBySessionId5xx: StubMapping = stubFor(
    get(urlPathEqualTo(findLatestBySessionIdPath)).willReturn(
      aResponse()
        .withStatus(Status.SERVICE_UNAVAILABLE)
    )
  )

  private def findLatestByJourneyIdPath(journeyId: JourneyId): String = s"/pay-api/journey/${journeyId.value}"

  def stubForFindByJourneyId2xx(journeyId: JourneyId)(journey: Journey[JourneySpecificData]): StubMapping = stubFor(
    get(urlPathEqualTo(findLatestByJourneyIdPath(journeyId))).willReturn(
      aResponse()
        .withStatus(Status.OK)
        .withBody(Json.prettyPrint(Json.toJson(journey)))
    )
  )

  def stubForFindByJourneyId404(journeyId: JourneyId): StubMapping = stubFor(
    get(urlPathEqualTo(findLatestByJourneyIdPath(journeyId))).willReturn(
      aResponse()
        .withStatus(Status.NOT_FOUND)
    )
  )

  def stubForFindByJourneyId5xx(journeyId: JourneyId): StubMapping = stubFor(
    get(urlPathEqualTo(findLatestByJourneyIdPath(journeyId))).willReturn(
      aResponse()
        .withStatus(Status.SERVICE_UNAVAILABLE)
    )
  )

  def verifyFindByJourneyId(count: Int = 0, journeyId: JourneyId): Unit = verify(
    count,
    getRequestedFor(urlEqualTo(findLatestByJourneyIdPath(journeyId)))
  )

  private def updateBeginWebPaymentPath(journeyId: String): String = s"/pay-api/journey/$journeyId/update/begin-web-payment"

  def verifyUpdateBeginWebPayment(count: Int = 1, journeyId: String): Unit = verify(
    count,
    putRequestedFor(urlEqualTo(updateBeginWebPaymentPath(journeyId)))
      .withRequestBody(equalToJson(
        """
          |{
          | "transactionReference": "sometransactionref",
          | "iFrameUrl":            "someiframeurl"
          |}
          |""".stripMargin,
        true,
        true
      ))
  )

  private def updateSucceedWebPaymentPath(journeyId: String): String = s"/pay-api/journey/$journeyId/update/succeed-web-payment"

  def verifyUpdateSucceedWebPayment(count: Int = 1, journeyId: String, expectedTransactionTime: LocalDateTime): Unit = verify(
    count,
    putRequestedFor(urlEqualTo(updateSucceedWebPaymentPath(journeyId)))
      .withRequestBody(equalToJson(
        s"""
          |{
          | "cardCategory": "debit",
          | "commissionInPence": 123,
          | "transactionTime": "${expectedTransactionTime.format(DateTimeFormatter.ISO_DATE_TIME)}"
          |}
          |""".stripMargin,
        true,
        true
      ))
  )

  private def updateFailWebPaymentPath(journeyId: String): String = s"/pay-api/journey/$journeyId/update/fail-web-payment"

  def verifyUpdateFailWebPayment(count: Int = 1, journeyId: String, expectedTransactionTime: LocalDateTime): Unit = verify(
    count,
    putRequestedFor(urlEqualTo(updateFailWebPaymentPath(journeyId)))
      .withRequestBody(equalToJson(
        s"""
          |{
          | "transactionTime": "${expectedTransactionTime.format(DateTimeFormatter.ISO_DATE_TIME)}",
          | "cardCategory": "debit"
          |}
          |""".stripMargin,
        true,
        true
      ))
  )

  private def updateCancelWebPaymentPath(journeyId: String): String = s"/pay-api/journey/$journeyId/update/cancel-web-payment"

  def verifyUpdateCancelWebPayment(count: Int = 1, journeyId: String): Unit = verify(
    count,
    putRequestedFor(urlEqualTo(updateCancelWebPaymentPath(journeyId)))
  )

}
