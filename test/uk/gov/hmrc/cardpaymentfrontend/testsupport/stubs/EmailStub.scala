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

package uk.gov.hmrc.cardpaymentfrontend.testsupport.stubs

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalToJson, post, postRequestedFor, stubFor, urlEqualTo, urlPathEqualTo, verify}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.cardpaymentfrontend.models.email.EmailRequest

object EmailStub {

  private val path: String = s"/hmrc/email"

  def verifyEmailWasNotSent(): Unit = verify(0, postRequestedFor(urlEqualTo(path)))

  def verifyEmailWasSent(expectedRequestBody: JsValue): Unit =
    verify(1, postRequestedFor(urlEqualTo(path)).withRequestBody(equalToJson(expectedRequestBody.toString())))

  def verifySomeEmailWasSent(): Unit = verify(1, postRequestedFor(urlEqualTo(path)))

  def stubForSendEmail(emailRequest: EmailRequest): StubMapping = stubFor(
    post(urlPathEqualTo(path)).willReturn(
      aResponse()
        .withStatus(Status.ACCEPTED)
        .withBody(Json.prettyPrint(Json.toJson(emailRequest)))
    )
  )

}
