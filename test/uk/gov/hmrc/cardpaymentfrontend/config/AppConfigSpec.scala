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

import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec

class AppConfigSpec extends ItSpec {

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  "AppConfig" - {
    "payAnotherWayLink" - {
      "should return the correct URL from the configuration" in {
        appConfig.payAnotherWayLink shouldBe "https://www.gov.uk/topic/dealing-with-hmrc/paying-hmrc"
      }
    }

    "cardPaymentBaseUrl" - {
      "should return the correct base URL from the configuration" in {
        appConfig.cardPaymentBaseUrl shouldBe "http://localhost:6001"
      }
    }

    "payApiBaseUrl" - {
      "should return the correct base URL from the configuration" in {
        appConfig.payApiBaseUrl shouldBe "http://localhost:6001"
      }
    }

    "openBankingBaseUrl" - {
      "should return the correct base URL from the configuration" in {
        appConfig.openBankingBaseUrl shouldBe "http://localhost:6001"
      }
    }

    "paymentsSurveyBaseUrl" - {
      "should return the correct base URL from the configuration" in {
        appConfig.paymentsSurveyBaseUrl shouldBe "http://localhost:6001"
      }
    }

    "emailBaseUrl" - {
      "should return the correct base URL from the configuration" in {
        appConfig.emailBaseUrl shouldBe "http://localhost:6001"
      }
    }

    "payFrontendBaseUrl" - {
      "should return the correct URL from the configuration" in {
        appConfig.payFrontendBaseUrl shouldBe "http://localhost:9056/pay"
      }
    }

    "cardPaymentFrontendBaseUrl" - {
      "should return the correct URL from the configuration" in {
        appConfig.cardPaymentFrontendBaseUrl shouldBe "http://localhost:10155"
      }
    }

    "bankTransferRelativeUrl" - {
      "should return the correct relative URL from the configuration" in {
        appConfig.bankTransferRelativeUrl shouldBe "/bac"
      }
    }

    "oneOffDirectDebitRelativeUrl" - {
      "should return the correct relative URL from the configuration" in {
        appConfig.oneOffDirectDebitRelativeUrl shouldBe "/pay-by-one-off-direct-debit"
      }
    }

    "iframeHostNameAllowList" - {
      "should return the correct set of hostnames from the configuration" in {
        appConfig.iframeHostNameAllowList shouldBe Set("localhost", "pp.eshapay.net")
      }
    }

    "useProductionClientIds" - {
      "should return the correct boolean value from the configuration" in {
        appConfig.useProductionClientIds shouldBe true
      }
    }

    "cardPaymentInternalAuthToken" - {
      "should return the correct token from the configuration" in {
        appConfig.cardPaymentInternalAuthToken shouldBe "testToken"
      }
    }
  }

}
