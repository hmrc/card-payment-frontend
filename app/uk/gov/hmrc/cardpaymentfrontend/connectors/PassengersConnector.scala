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
import play.api.libs.json.Json
import play.api.libs.ws.writeableOf_JsValue
import uk.gov.hmrc.cardpaymentfrontend.config.AppConfig
import uk.gov.hmrc.cardpaymentfrontend.models.notifications.PassengersNotification
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.http.HttpReads.Implicits.*
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class PassengersConnector @Inject() (appConfig: AppConfig, httpClientV2: HttpClientV2)(implicit executionContext: ExecutionContext) extends Logging {

  private val baseUrl: String = appConfig.passengersBaseUrl + "/bc-passengers-declarations"
  private val notificationUrl = url"$baseUrl/update-payment"

  def sendNotification(passengersNotification: PassengersNotification)(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] =
    httpClientV2
      .post(notificationUrl)
      .withBody(Json.toJson(passengersNotification))
      .execute[HttpResponse]
      .andThen {
        case Success(response) =>
          response.status match {
            case s if Status.isSuccessful(s) =>
              logger.info(s"[PassengersConnector] [POST ${notificationUrl.toString}] Successfully sent notification to passengers")
            case s                           =>
              logger.error(
                s"[PassengersConnector] [POST ${notificationUrl.toString}]  There was a problem sending notification to passengers, got a ${s.toString} status response"
              )
          }
        case Failure(e)        =>
          logger.error(s"[PassengersConnector] [POST ${notificationUrl.toString}] There was a problem sending notification to passengers", e)
      }

}
