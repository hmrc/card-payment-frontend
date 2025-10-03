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
import uk.gov.hmrc.cardpaymentfrontend.models.cds.{CdsResponse, GetCashDepositSubscriptionDetailsResponse, ResponseCommon, ResponseDetail}
import uk.gov.hmrc.cardpaymentfrontend.models.notifications.{CdsNotification, NotifyImmediatePaymentRequest, RequestCommon, RequestDetail}
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec
import uk.gov.hmrc.cardpaymentfrontend.testsupport.stubs.CdsStub
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.TestPayApiData
import uk.gov.hmrc.http.HeaderCarrier

class CdsConnectorSpec extends ItSpec {

  private val systemUnderTest: CdsConnector = app.injector.instanceOf[CdsConnector]
  private val headerCarrier: HeaderCarrier = HeaderCarrier()

  "getCashDepositSubscriptionDetails" - {
    "should return a CdsResponse" in {
      CdsStub.stubGetCashDepositSubscriptionDetail2xx(TestPayApiData.testCdsRef)
      val result = systemUnderTest.getCashDepositSubscriptionDetails(TestPayApiData.testCdsRef)(headerCarrier).futureValue
      result shouldBe CdsResponse(GetCashDepositSubscriptionDetailsResponse(ResponseCommon("Ok", "2018-09-24T11:01:01Z"), ResponseDetail("16NLIWQ2W3AXAGWD52", "CDSI191234567890", "2018-09-24T11:01:01Z", false, Some("2018-09-24T11:01:01Z"))))

    }
    "should error when cds service errors" in {
      CdsStub.stubGetCashDepositSubscriptionDetail5xx(TestPayApiData.testCdsRef)
      val error: Exception = intercept[Exception](systemUnderTest.getCashDepositSubscriptionDetails(TestPayApiData.testCdsRef)(headerCarrier).futureValue)
      error.getCause.getMessage should include(s"GET of 'http://127.0.0.1:${wireMockPort.toString}/accounts/getcashdepositsubscriptiondetails/v1?paymentReference=CDSI191234567890' returned 503.")
    }
  }

  "sendNotification" - {

    val testCdsNotification = CdsNotification(
      notifyImmediatePaymentRequest = NotifyImmediatePaymentRequest(
        requestCommon = RequestCommon(
          receiptDate              = "testReceiptDate",
          acknowledgementReference = "testAcknowledgementReference"
        ),
        requestDetail = RequestDetail(
          paymentReference = "testPaymentReference",
          amountPaid       = "testAmountPaid",
          declarationID    = "testDeclarationID"
        )
      )
    )

    "should return a HttpResponse with a status" in {
      CdsStub.stubNotification2xx(Json.toJson(testCdsNotification))
      val result = systemUnderTest.sendNotification(testCdsNotification)(TestPayApiData.testTransactionReference)(headerCarrier).futureValue
      result.status shouldBe 200
    }
    "should return HttpResonse with error code status when cds errors" in {
      CdsStub.stubNotification5xx()
      val result = systemUnderTest.sendNotification(testCdsNotification)(TestPayApiData.testTransactionReference)(headerCarrier).futureValue
      result.status shouldBe 503
    }
  }

}
