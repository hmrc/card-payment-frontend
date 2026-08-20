/*
 * Copyright 2026 HM Revenue & Customs
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

import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec
import uk.gov.hmrc.cardpaymentfrontend.testsupport.stubs.PaymentsBasketStub
import uk.gov.hmrc.http.HeaderCarrier

class PaymentsBasketServiceSpec extends ItSpec {

  val systemUnderTest: PaymentsBasketService = app.injector.instanceOf[PaymentsBasketService]

  "PaymentsBasketService" - {
    "notifyEtmpIfBasket" - {
      "should return Right(()) when the ETMP notification is successful" in {
        val basketReference = "XBKT123456789"
        PaymentsBasketStub.notifyEtmp(basketReference)

        implicit val hc: HeaderCarrier = HeaderCarrier()
        val result                     = systemUnderTest.notifyEtmpIfBasket(basketReference).futureValue
        result shouldBe Right(())
      }

      "should return Left with error message when the ETMP notification fails" in {
        val basketReference = "XBKT987654321"
        PaymentsBasketStub.notifyEtmp(basketReference, status = 400)

        implicit val hc: HeaderCarrier = HeaderCarrier()
        val result                     = systemUnderTest.notifyEtmpIfBasket(basketReference).futureValue
        result match {
          case Left(errorMsg) => errorMsg should include("Failed to notify ETMP")
          case Right(_)       => fail("Expected Left but got Right")
        }
      }
    }
  }

}
