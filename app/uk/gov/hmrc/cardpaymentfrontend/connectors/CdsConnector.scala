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
import payapi.corcommon.model.barclays.TransactionReference
import payapi.corcommon.model.taxes.cds.CdsRef
import play.api.Logging
import play.api.http.Status
import play.api.libs.json.Json
import uk.gov.hmrc.cardpaymentfrontend.config.AppConfig
import uk.gov.hmrc.cardpaymentfrontend.models.cds.CdsResponse
import uk.gov.hmrc.cardpaymentfrontend.models.notifications.CdsNotification
import uk.gov.hmrc.http.HttpReadsInstances._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class CdsConnector @Inject() (appConfig: AppConfig, httpClientV2: HttpClientV2)(implicit executionContext: ExecutionContext) extends Logging {

  private val authHeadersToUse = Seq(("Authorization", s"Bearer ${appConfig.cdsAuthToken}"))
  private val baseUrl: String  = appConfig.cdsBaseUrl + "/accounts"
  private def cdsReferenceCheckUrl(cdsRef: CdsRef) = url"$baseUrl/getcashdepositsubscriptiondetails/v1?paymentReference=${cdsRef.value}"
  private val cdsNotificationUrl = url"$baseUrl/notifyimmediatepayment/v1"

  def getCashDepositSubscriptionDetails(cdsRef: CdsRef)(implicit headerCarrier: HeaderCarrier): Future[CdsResponse] =
    httpClientV2
      .get(url"${cdsReferenceCheckUrl(cdsRef)}")
      .setHeader(authHeadersToUse: _*)
      .execute[CdsResponse]

  def sendNotification(
    cdsNotification: CdsNotification
  )(journeyTransactionReference: TransactionReference)(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = {
    val headers: Seq[(String, String)] = authHeadersToUse :+ ("X-Correlation-ID" -> journeyTransactionReference.value) :+ ("Accept" -> "application/json")
    httpClientV2
      .post(cdsNotificationUrl)
      .setHeader(headers: _*)
      .withBody(Json.toJson(cdsNotification))
      .execute[HttpResponse]
      .andThen {
        case Success(response) =>
          response.status match {
            case s if Status.isSuccessful(s) =>
              logger.info(
                s"[CdsConnector] [POST ${cdsNotificationUrl.toString}] [ackRef:${cdsNotification.notifyImmediatePaymentRequest.requestCommon.acknowledgementReference}] Successfully sent notification to CDS"
              )
            case s                           =>
              logger.error(
                s"[CdsConnector] [POST ${cdsNotificationUrl.toString}] [ackRef:${cdsNotification.notifyImmediatePaymentRequest.requestCommon.acknowledgementReference}] There was a problem sending notification to CDS, got a ${s.toString} status response"
              )
          }
        case Failure(e)        =>
          logger.error(
            s"[CdsConnector] [POST ${cdsNotificationUrl.toString}] [ackRef:${cdsNotification.notifyImmediatePaymentRequest.requestCommon.acknowledgementReference}] There was a problem sending notification to CDS",
            e
          )
      }
  }

}
