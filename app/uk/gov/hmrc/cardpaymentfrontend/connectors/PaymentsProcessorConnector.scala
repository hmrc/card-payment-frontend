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

import com.google.inject.{Inject, Singleton}
import play.api.Logging
import play.api.http.Status
import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.cardpaymentfrontend.config.AppConfig
import uk.gov.hmrc.cardpaymentfrontend.models.notifications.{ModsNotification, NotificationLoggingContext}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.http.HttpReads.Implicits._

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class PaymentsProcessorConnector @Inject() (appConfig: AppConfig, httpClientV2: HttpClientV2)(implicit executionContext: ExecutionContext) extends Logging {

  private val paymentsProcessorBaseUrl: String = appConfig.paymentsProcessorBaseUrl + "/payments-processor"
  private val modsNotificationUrl: URL         = url"$paymentsProcessorBaseUrl/mib/payment-callback"

  def sendModsNotification(modsNotification: ModsNotification)(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] =
    sendNotificationToPaymentsProcessor(modsNotificationUrl, modsNotification)(NotificationLoggingContext.modsNotificationLoggingContext)

  private def sendNotificationToPaymentsProcessor[N](endpoint: URL, notification: N)(
    notificationLoggingContext: NotificationLoggingContext
  )(implicit writes: Writes[N], headerCarrier: HeaderCarrier): Future[HttpResponse] =
    httpClientV2
      .post(endpoint)
      .withBody(Json.toJson(notification))
      .execute[HttpResponse]
      .andThen {
        case Success(response) =>
          response.status match {
            case s if Status.isSuccessful(s) =>
              logger.info(s"[PaymentsProcessorConnector] [POST ${endpoint.toString}] ${notificationLoggingContext.successMessage}")
            case s                           =>
              logger.error(
                s"[PaymentsProcessorConnector] [POST ${endpoint.toString}] ${notificationLoggingContext.unexpectedStatusMessage}, got a ${s.toString} status response"
              )
          }
        case Failure(e)        =>
          logger.error(s"[PaymentsProcessorConnector] [POST ${endpoint.toString}] ${notificationLoggingContext.failureMessage}", e)
      }

}
