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

import play.api.libs.json.{JsBoolean, Json}
import uk.gov.hmrc.cardpaymentfrontend.config.AppConfig
import uk.gov.hmrc.cardpaymentfrontend.models.cardpayment.{CardPaymentInitiatePaymentRequest, CardPaymentInitiatePaymentResponse}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.HttpReads.Implicits._

import java.net.URL
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CardPaymentConnector @Inject() (appConfig: AppConfig, httpClientV2: HttpClientV2)(implicit executionContext: ExecutionContext) {

  private val cardPaymentBaseUrl: URL = url"""${appConfig.cardPaymentBaseUrl}"""
  private val initiatePaymentUrl: URL = url"$cardPaymentBaseUrl/card-payment/initiate-payment"
  private val checkPaymentStatusUrl: String => URL = (transactionReference: String) => url"$cardPaymentBaseUrl/card-payment/payment-status/$transactionReference"
  private val authAndSettleUrl: String => URL = (transactionReference: String) => url"$cardPaymentBaseUrl/card-payment/auth-and-settle/$transactionReference"

  def initiatePayment(cardPaymentInitiatePaymentRequest: CardPaymentInitiatePaymentRequest)(implicit headerCarrier: HeaderCarrier): Future[CardPaymentInitiatePaymentResponse] =
    httpClientV2
      .post(initiatePaymentUrl)
      .withBody(Json.toJson(cardPaymentInitiatePaymentRequest))
      .execute[CardPaymentInitiatePaymentResponse]

  //todo probably delete?
  def checkPaymentStatus(transactionReference: String)(implicit headerCarrier: HeaderCarrier): Future[JsBoolean] =
    httpClientV2
      .get(checkPaymentStatusUrl(transactionReference))
      .execute[JsBoolean]

  //todo should we make this strongly typed and return a CardPaymentResult or Option[CardPaymentResult]?
  def authAndSettle(transactionReference: String)(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] =
    httpClientV2
      .post(authAndSettleUrl(transactionReference))
      .execute[HttpResponse]
}
