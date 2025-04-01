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

package uk.gov.hmrc.cardpaymentfrontend.connectors

import com.google.inject.Inject
import payapi.cardpaymentjourney.model.journey.{Journey, JourneySpecificData}
import play.api.libs.json.Json
import uk.gov.hmrc.cardpaymentfrontend.config.AppConfig
import uk.gov.hmrc.cardpaymentfrontend.models.payapi.{BeginWebPaymentRequest, FailWebPaymentRequest, SucceedWebPaymentRequest}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.StringContextOps
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.HttpReads.Implicits._

import java.net.URL
import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PayApiConnector @Inject() (appConfig: AppConfig, httpClientV2: HttpClientV2)(implicit executionContext: ExecutionContext) {

  private val findBySessionIdUrl: URL = url"""${appConfig.payApiBaseUrl}/pay-api/journey/find-latest-by-session-id"""

  def findLatestJourneyBySessionId()(implicit headerCarrier: HeaderCarrier): Future[Option[Journey[JourneySpecificData]]] = {
    for {
      _ <- Future(require(headerCarrier.sessionId.isDefined, "Missing required 'SessionId'"))
      maybeJourneyResult <- httpClientV2.get(findBySessionIdUrl).execute[Option[Journey[JourneySpecificData]]]
    } yield maybeJourneyResult
  }

  object JourneyUpdates {

    def updateBeginWebPayment(journeyId: String, beginWebPaymentRequest: BeginWebPaymentRequest)(implicit headerCarrier: HeaderCarrier): Future[Unit] =
      httpClientV2
        .put(url"""${appConfig.payApiBaseUrl}/pay-api/journey/$journeyId/update/begin-web-payment""")
        .withBody(Json.toJson(beginWebPaymentRequest))
        .execute[Unit]

    def updateSucceedWebPayment(journeyId: String, succeedWebPaymentRequest: SucceedWebPaymentRequest)(implicit headerCarrier: HeaderCarrier): Future[Unit] =
      httpClientV2
        .put(url"""${appConfig.payApiBaseUrl}/pay-api/journey/$journeyId/update/succeed-web-payment""")
        .withBody(Json.toJson(succeedWebPaymentRequest))
        .execute[Unit]

    def updateCancelWebPayment(journeyId: String)(implicit headerCarrier: HeaderCarrier): Future[Unit] =
      httpClientV2
        .put(url"""${appConfig.payApiBaseUrl}/pay-api/journey/$journeyId/update/cancel-web-payment""")
        .execute[Unit]

    def updateFailWebPayment(journeyId: String, failWebPaymentRequest: FailWebPaymentRequest)(implicit headerCarrier: HeaderCarrier): Future[Unit] =
      httpClientV2
        .put(url"""${appConfig.payApiBaseUrl}/pay-api/journey/$journeyId/update/fail-web-payment""")
        .withBody(Json.toJson(failWebPaymentRequest))
        .execute[Unit]
  }

}
