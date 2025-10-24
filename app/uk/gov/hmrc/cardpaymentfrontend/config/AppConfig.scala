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

package uk.gov.hmrc.cardpaymentfrontend.config

import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject() (config: Configuration, servicesConfig: ServicesConfig) {

  val payAnotherWayLink: String = config.get[String]("urls.govuk.pay-another-way")

  val cardPaymentBaseUrl: String = servicesConfig.baseUrl("card-payment")
  val payApiBaseUrl: String = servicesConfig.baseUrl("pay-api")
  val openBankingBaseUrl: String = servicesConfig.baseUrl("open-banking")
  val paymentsSurveyBaseUrl: String = servicesConfig.baseUrl("payments-survey")
  val emailBaseUrl: String = servicesConfig.baseUrl("email-service")

  private val platformHost: Option[String] = config.getOptional[String]("platform.frontend.host")
  private val basGatewayBaseUrl: String = platformHost.getOrElse(config.get[String]("urls.bas-gateway.base-url"))

  val signOutUrl: String = s"$basGatewayBaseUrl/bas-gateway/sign-out-without-state"

  val businessTaxAccLoginMGDOrigin: String = s"$basGatewayBaseUrl/gg/sign-in?continue=/business-account/&origin=MGD-frontend"

  val signInUrl: String = config.get[String]("urls.sign-in.base-url")

  val timeoutInSeconds: Int = config.get[Int]("timeoutInSeconds")
  val countdownInSeconds: Int = config.get[Int]("countdownInSeconds")

  val payFrontendBaseUrl: String = config.get[String]("urls.pay-frontend.base-url") + "/pay"
  val cardPaymentFrontendBaseUrl: String = config.get[String]("urls.card-payment-frontend.base-url")
  val businessTaxAccountUrl: String = config.get[String]("urls.business-tax-account.base-url") + "/business-account"
  val merchandiseInBaggageFrontendBaseUrl: String = servicesConfig.baseUrl("merchandise-in-baggage-frontend") + "/declare-commercial-goods"
  val merchandiseInBaggageMakeAnotherDeclarationUrl: String = merchandiseInBaggageFrontendBaseUrl + "/make-another-declaration"
  val merchandiseInBaggageAmendDeclarationUrl: String = merchandiseInBaggageFrontendBaseUrl + "/add-goods-to-an-existing-declaration"
  val merchandiseInBaggageSurveyUrl: String = merchandiseInBaggageFrontendBaseUrl + "/survey"

  val bankTransferRelativeUrl: String = config.get[String]("urls.pay-frontend.bank-transfer")
  val oneOffDirectDebitRelativeUrl: String = config.get[String]("urls.pay-frontend.one-off-direct-debit")
  val variableDirectDebitRelativeUrl: String = config.get[String]("urls.pay-frontend.variable-direct-debit")
  val directDebitRelativeUrl: String = config.get[String]("urls.pay-frontend.direct-debit")

  val iframeHostNameAllowList: Set[String] = config.get[Seq[String]]("iframeHostNameAllowList").toSet
  val useProductionClientIds: Boolean = servicesConfig.getBoolean("use-production-client-ids")

  val cardPaymentInternalAuthToken: String = servicesConfig.getString("internal-auth.token")

  val cdsBaseUrl: String = servicesConfig.baseUrl("cds")
  val cdsAuthToken: String = servicesConfig.getString("microservice.services.cds.auth-token")

  val paymentsProcessorBaseUrl: String = servicesConfig.baseUrl("payments-processor")
  val passengersBaseUrl: String = servicesConfig.baseUrl("bc-passengers-declarations")
}
