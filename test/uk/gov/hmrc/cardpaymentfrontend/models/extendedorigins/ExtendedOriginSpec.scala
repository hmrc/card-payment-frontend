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

package uk.gov.hmrc.cardpaymentfrontend.models.extendedorigins

import payapi.corcommon.model.AmountInPence
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec
import uk.gov.hmrc.cardpaymentfrontend.testsupport.TestOps.FakeRequestOps
import uk.gov.hmrc.cardpaymentfrontend.testsupport.stubs.PayApiStub
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.TestJourneys

class ExtendedOriginSpec extends ItSpec {
  private val systemUnderTest = ExtendedBtaSa //ExtendedBtaSa is a concrete reification of the trait ExtendedOrigin, we use it as a substitute here.
  private val fakeGetRequest = FakeRequest("GET", "/cya0").withSessionId()
  private val testJourney = TestJourneys.BtaSa.testBtaSaJourneySuccessDebit

  "A value with pounds and pennies will display the pounds and pennies to 2 decimal places" in {
    PayApiStub.stubForFindBySessionId2xx(testJourney)
    val fakeJourneyRequest: JourneyRequest[AnyContent] = new JourneyRequest(testJourney, fakeGetRequest)
    val result: String = systemUnderTest.asInstanceOf[ExtendedOrigin].amount(fakeJourneyRequest)
    result shouldBe "£12.34"
  }

  "A value with pounds but no pennies will display the pounds only and no decimal places" in {
    val testJourneyWithFivePounds = testJourney copy (amountInPence = Some(AmountInPence(500)))
    val fakeJourneyRequest: JourneyRequest[AnyContent] = new JourneyRequest(testJourneyWithFivePounds, fakeGetRequest)
    val result: String = systemUnderTest.asInstanceOf[ExtendedOrigin].amount(fakeJourneyRequest)
    result shouldBe "£5"
  }
}
