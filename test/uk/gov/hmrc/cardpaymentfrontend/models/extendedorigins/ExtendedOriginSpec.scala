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

import payapi.corcommon.model.{AmountInPence, FutureDatedPayment}
import play.api.mvc.{AnyContent, Call}
import play.api.test.FakeRequest
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.CheckYourAnswersRow
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec
import uk.gov.hmrc.cardpaymentfrontend.testsupport.TestOps.FakeRequestOps
import uk.gov.hmrc.cardpaymentfrontend.testsupport.stubs.PayApiStub
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.TestJourneys
import uk.gov.hmrc.cardpaymentfrontend.models.Link

import java.time.LocalDate

class ExtendedOriginSpec extends ItSpec {
  private val systemUnderTest = ExtendedBtaSa //ExtendedBtaSa is a concrete reification of the trait ExtendedOrigin, we use it as a substitute here.
  private val fakeGetRequest = FakeRequest("GET", "/cya0").withSessionId()
  private val testJourney = TestJourneys.BtaSa.testBtaSaJourneySuccessDebit
  private val testJourneyNoDueDate = TestJourneys.BtaSa.testBtaSaJourneySuccessDebitNoDueDate
  private val testJourneyOverdue = TestJourneys.BtaSa.testBtaSaJourneySuccessDebitNoDueDate

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

  "showFuturePayment" - {

    "should return true when dueDate in journeySpecificData is in the future" in {
      val fakeJourneyRequest: JourneyRequest[AnyContent] = new JourneyRequest(testJourney, fakeGetRequest)
      systemUnderTest.showFuturePayment(fakeJourneyRequest) shouldBe true
    }
    "should return false when dueDate in journeySpecificData is None" in {
      val fakeJourneyRequest: JourneyRequest[AnyContent] = new JourneyRequest(testJourneyNoDueDate, fakeGetRequest)
      systemUnderTest.showFuturePayment(fakeJourneyRequest) shouldBe false
    }
    "should return false when dueDate in journeySpecificData is Some value in the past" in {
      val fakeJourneyRequest: JourneyRequest[AnyContent] = new JourneyRequest(testJourneyOverdue, fakeGetRequest)
      systemUnderTest.showFuturePayment(fakeJourneyRequest) shouldBe false
    }

  }

  "checkYourAnswersPaymentDateRow" - {
    "return Some[CheckYourAnswersRow] when showFuturePayment returns true" in {
      val testJourneyWithFutureDatedPayment = testJourney copy (futureDatedPayment = Some(FutureDatedPayment(LocalDate.now().plusMonths(1))))
      val fakeJourneyRequest: JourneyRequest[AnyContent] = new JourneyRequest(testJourneyWithFutureDatedPayment, fakeGetRequest)
      val result: Option[CheckYourAnswersRow] = systemUnderTest.checkYourAnswersPaymentDateRow(fakeJourneyRequest)
      result shouldBe Some(CheckYourAnswersRow("check-your-details.payment-date", List("check-your-details.payment-date.today"), Some(Link(Call("GET", "some-link-to-pay-frontend"), "check-your-details-payment-date-change-link", "check-your-details.change", None))))

    }
    "return None when showFuturePayment returns false" in {
      val fakeJourneyRequest: JourneyRequest[AnyContent] = new JourneyRequest(testJourneyNoDueDate, fakeGetRequest)
      val result: Option[CheckYourAnswersRow] = systemUnderTest.checkYourAnswersPaymentDateRow(fakeJourneyRequest)
      result shouldBe (None)
    }
  }
}
