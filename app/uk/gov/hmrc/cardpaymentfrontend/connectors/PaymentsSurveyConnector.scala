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

import play.api.libs.json.Json
import uk.gov.hmrc.cardpaymentfrontend.config.AppConfig
import uk.gov.hmrc.cardpaymentfrontend.models.paymentssurvey.{PaymentSurveyJourneyRequest, SsjResponse}
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.HttpReads.Implicits._

import java.net.URL
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PaymentsSurveyConnector @Inject() (
    appConfig:  AppConfig,
    httpClient: HttpClientV2
)(
    implicit
    ec: ExecutionContext
) {

  private val serviceURL: URL = url"${appConfig.paymentsSurveyBaseUrl}/payments-survey/journey/start"

  def startSurvey(surveyRequest: PaymentSurveyJourneyRequest)(implicit headerCarrier: HeaderCarrier): Future[SsjResponse] = {
    httpClient.post(serviceURL).withBody(Json.toJson(surveyRequest)).execute[SsjResponse]
  }
}
