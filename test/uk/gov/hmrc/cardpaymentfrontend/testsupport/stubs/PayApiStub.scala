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

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import payapi.cardpaymentjourney.model.journey.{Journey, JourneySpecificData}
import play.api.http.Status
import play.api.libs.json.Json

object PayApiStub {

  private val path: String = s"/pay-api/journey/find-latest-by-session-id"

  def stubForFindBySessionId2xx(journey: Journey[JourneySpecificData]): StubMapping = stubFor(
    get(urlPathEqualTo(path)).willReturn(
      aResponse()
        .withStatus(Status.OK)
        .withBody(Json.prettyPrint(Json.toJson(journey)))
    )
  )

  def stubForFindBySessionId404: StubMapping = stubFor(
    get(urlPathEqualTo(path)).willReturn(
      aResponse()
        .withStatus(Status.NOT_FOUND)
    )
  )

  def stubForFindBySessionId5xx: StubMapping = stubFor(
    get(urlPathEqualTo(path)).willReturn(
      aResponse()
        .withStatus(Status.SERVICE_UNAVAILABLE)
    )
  )

}
