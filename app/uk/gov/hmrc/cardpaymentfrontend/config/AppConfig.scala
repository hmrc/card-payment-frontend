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

package uk.gov.hmrc.cardpaymentfrontend.config

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class AppConfig @Inject() (config: Configuration, servicesConfig: ServicesConfig) {

  val payAnotherWayLink: String = config.get[String]("urls.govuk.pay-another-way")

  val payApiBaseUrl: String = servicesConfig.baseUrl("pay-api")
  val openBankingBaseUrl: String = servicesConfig.baseUrl("open-banking")
  val paymentsSurveyBaseUrl: String = servicesConfig.baseUrl("payments-survey")
  val emailUrl: String = servicesConfig.baseUrl("email-service")

  val payFrontendBaseUrl: String = config.get[String]("urls.pay-frontend.base-url") + "/pay"

  val bankTransferRelativeUrl: String = config.get[String]("urls.pay-frontend.bank-transfer")
  val oneOffDirectDebitRelativeUrl: String = config.get[String]("urls.pay-frontend.one-off-direct-debit")

  val vatOssUrl: String = s"${config.get[String]("urls.vatOssBaseUrl")}/pay-vat-on-goods-sold-to-eu/northern-ireland-returns-payments/your-account"
  val vatIossUrl: String = s"${config.get[String]("urls.vatIossBaseUrl")}/pay-vat-on-goods-sold-to-eu/import-one-stop-shop-returns-payments/your-account"

}
