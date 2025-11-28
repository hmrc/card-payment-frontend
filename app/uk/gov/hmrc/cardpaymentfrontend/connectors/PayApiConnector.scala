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

import com.google.inject.Inject
import payapi.cardpaymentjourney.model.journey.{Journey, JourneySpecificData}
import payapi.corcommon.model.JourneyId
import play.api.Logging
import play.api.libs.json.Json
import uk.gov.hmrc.cardpaymentfrontend.config.AppConfig
import uk.gov.hmrc.cardpaymentfrontend.models.payapirequest.{BeginWebPaymentRequest, FailWebPaymentRequest, SucceedWebPaymentRequest}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2

import java.net.URL
import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class PayApiConnector @Inject() (appConfig: AppConfig, httpClientV2: HttpClientV2)(implicit executionContext: ExecutionContext) extends Logging {

  private val findBySessionIdUrl: URL = url"""${appConfig.payApiBaseUrl}/pay-api/journey/find-latest-by-session-id"""
  private def findJourneyByJourneyIdUrl(journeyId: JourneyId): URL = url"""${appConfig.payApiBaseUrl}/pay-api/journey/${journeyId.value}"""

  def findLatestJourneyBySessionId()(implicit headerCarrier: HeaderCarrier): Future[Option[Journey[JourneySpecificData]]] = {
    for {
      _ <- Future(require(headerCarrier.sessionId.isDefined, "Missing required 'SessionId'"))
      maybeJourneyResult <- httpClientV2.get(findBySessionIdUrl).execute[Option[Journey[JourneySpecificData]]]
    } yield maybeJourneyResult
  }

  def findJourneyByJourneyId(journeyId: JourneyId)(implicit headerCarrier: HeaderCarrier): Future[Option[Journey[JourneySpecificData]]] = {
    for {
      maybeJourneyResult <- httpClientV2.get(findJourneyByJourneyIdUrl(journeyId)).execute[Option[Journey[JourneySpecificData]]]
    } yield maybeJourneyResult
  }

  object JourneyUpdates {

    def updateBeginWebPayment(journeyId: String, beginWebPaymentRequest: BeginWebPaymentRequest)(implicit headerCarrier: HeaderCarrier): Future[Unit] =
      httpClientV2
        .put(url"""${appConfig.payApiBaseUrl}/pay-api/journey/$journeyId/update/begin-web-payment""")
        .withBody(Json.toJson(beginWebPaymentRequest))
        .execute[Unit]
        .andThen {
          case Failure(exception) => logger.error(s"Failed to update pay-api with BeginWebPaymentRequest: ${exception.getCause.toString}")
          case Success(_)         => logger.debug("Successfully updated pay-api with BeginWebPaymentRequest")
        }

    def updateSucceedWebPayment(journeyId: String, succeedWebPaymentRequest: SucceedWebPaymentRequest)(implicit headerCarrier: HeaderCarrier): Future[Unit] =
      httpClientV2
        .put(url"""${appConfig.payApiBaseUrl}/pay-api/journey/$journeyId/update/succeed-web-payment""")
        .withBody(Json.toJson(succeedWebPaymentRequest))
        .execute[Unit]
        .andThen {
          case Failure(exception) => logger.error(s"Failed to update pay-api with SucceedWebPaymentRequest: ${exception.getCause.toString}")
          case Success(_)         => logger.debug("Successfully updated pay-api with SucceedWebPaymentRequest")
        }

    def updateCancelWebPayment(journeyId: String)(implicit headerCarrier: HeaderCarrier): Future[Unit] =
      httpClientV2
        .put(url"""${appConfig.payApiBaseUrl}/pay-api/journey/$journeyId/update/cancel-web-payment""")
        .execute[Unit]
        .andThen {
          case Failure(exception) => logger.error(s"Failed to update pay-api for updateCancelWebPayment: ${exception.getCause.toString}")
          case Success(_)         => logger.debug("Successfully updated pay-api with updateCancelWebPayment")
        }

    def updateFailWebPayment(journeyId: String, failWebPaymentRequest: FailWebPaymentRequest)(implicit headerCarrier: HeaderCarrier): Future[Unit] =
      httpClientV2
        .put(url"""${appConfig.payApiBaseUrl}/pay-api/journey/$journeyId/update/fail-web-payment""")
        .withBody(Json.toJson(failWebPaymentRequest))
        .execute[Unit]
        .andThen {
          case Failure(exception) => logger.error(s"Failed to update pay-api with FailWebPaymentRequest: ${exception.getCause.toString}")
          case Success(_)         => logger.debug("Successfully updated pay-api with FailWebPaymentRequest")
        }

    def resetWebPayment(journeyId: String)(implicit headerCarrier: HeaderCarrier): Future[Unit] =
      httpClientV2
        .delete(url"${appConfig.payApiBaseUrl}/pay-api/journey/$journeyId/update/reset-web-payment")
        .execute[Unit]
        .andThen {
          case Failure(exception) => logger.error(s"Failed to reset web payment in pay-api: ${exception.getCause.toString}")
          case Success(_)         => logger.debug("Successfully reset web payment in pay-api")
        }
  }

  def restartJourneyAsNew(journeyId: JourneyId)(implicit headerCarrier: HeaderCarrier): Future[JourneyId] =
    httpClientV2
      .post(url"${appConfig.payApiBaseUrl}/pay-api/journey/${journeyId.value}/restart-journey-as-new")
      .execute[JourneyId]
      .andThen {
        case Failure(exception) => logger.error(s"Failed to clone journey in pay-api: ${exception.getMessage}")
        case Success(id)        => logger.debug(s"Successfully cloned journey in pay-api, new journeyId: [ ${id.value} ]")
      }

}
