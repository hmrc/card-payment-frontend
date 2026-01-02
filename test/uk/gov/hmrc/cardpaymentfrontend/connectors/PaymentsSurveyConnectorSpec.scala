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

import payapi.cardpaymentjourney.model.journey.Url
import uk.gov.hmrc.cardpaymentfrontend.models.paymentssurvey._
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec
import uk.gov.hmrc.cardpaymentfrontend.testsupport.stubs.PaymentsSurveyStub
import uk.gov.hmrc.http.HeaderCarrier

class PaymentsSurveyConnectorSpec extends ItSpec {
  val systemUnderTest: PaymentsSurveyConnector = app.injector.instanceOf[PaymentsSurveyConnector]
  "PaymentsSurveyConnector" - {
    "startPaySurvey" - {
      val paymentSurveyJourneyRequest = PaymentSurveyJourneyRequest(
        origin = "some-origin",
        returnMsg = "some-returnMsg",
        returnHref = "some-returnHref",
        auditName = "some-auditName",
        audit = AuditOptions(
          userType = "some-userType",
          journey = None,
          orderId = None,
          liability = None
        ),
        contentOptions = SurveyContentOptions(
          isWelshSupported = true,
          title = SurveyBannerTitle("Pay your tax")
        )
      )
      "propagate a 5xx error when payments-survey returns a 5xx" in {
        PaymentsSurveyStub.stubForStartJourney5xx()
        implicit val hc: HeaderCarrier = HeaderCarrier()
        val error: Exception           = intercept[Exception](systemUnderTest.startSurvey(paymentSurveyJourneyRequest).futureValue)
        error.getCause.getMessage should include(s"POST of 'http://localhost:${wireMockPort.toString}/payments-survey/journey/start' returned 503.")
      }

      "return an SsjResponse" in {
        val ssjResponse                = SsjResponse(SurveyJourneyId("test-survey-journey-id"), Url("https://www.some-next-url.com"))
        PaymentsSurveyStub.stubForStartJourney2xx(ssjResponse)
        implicit val hc: HeaderCarrier = HeaderCarrier()
        systemUnderTest.startSurvey(paymentSurveyJourneyRequest).futureValue shouldBe ssjResponse
      }
    }
  }

}
