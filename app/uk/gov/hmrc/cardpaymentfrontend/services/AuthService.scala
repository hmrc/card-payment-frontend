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

package uk.gov.hmrc.cardpaymentfrontend.services

import uk.gov.hmrc.cardpaymentfrontend.config.AppConfig
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.hmrcstandardpage.ServiceURLs

import javax.inject.{Inject, Singleton}

@Singleton
final class AuthService @Inject() (appConfig: AppConfig) {
  private val ggBaseUrl: String = appConfig.ggBaseUrl
  private val defaultSignOutUrl: String = s"$ggBaseUrl/gg/sign-out?continue="

  val serviceURLs: ServiceURLs = ServiceURLs(
    signOutUrl = Some(defaultSignOutUrl)
  )

}
