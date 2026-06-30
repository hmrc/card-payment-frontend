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

package uk.gov.hmrc.cardpaymentfrontend.views

import org.jsoup.Jsoup
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout}
import uk.gov.hmrc.cardpaymentfrontend.controllers.PaymentCompleteController
import uk.gov.hmrc.cardpaymentfrontend.testsupport.TestOps.FakeRequestOps
import uk.gov.hmrc.cardpaymentfrontend.testsupport.stubs.PayApiStub
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.TestJourneys
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec

import scala.jdk.CollectionConverters.CollectionHasAsScala

class PaymentCompletePageSpec extends ItSpec {

  "Payment Complete Page" - {
    val systemUnderTest                                     = app.injector.instanceOf[PaymentCompleteController]
    val fakeGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/payment-complete").withSessionId()

    "render a View everything included in this basket link for StampTaxesOnShares basket journeys" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.StampTaxesOnShares.journeyAfterSucceedDebitWebPayment)

      val doc = Jsoup.parse(contentAsString(systemUnderTest.renderPage(fakeGetRequest))).body()
      doc.select("a.govuk-link").asScala.toList.size shouldBe 4
      doc.select("#summary").text() shouldBe "View everything included in this basket (opens in new tab)"
      doc.select("#summary").attr("href") shouldBe "#"
    }

    "not render a View everything included in this basket link for StampTaxesOnShares single payment journeys" in {
      PayApiStub.stubForFindBySessionId2xx(
        TestJourneys.StampTaxesOnShares.journeyAfterSucceedDebitWebPayment.copy(
          journeySpecificData = TestJourneys.StampTaxesOnShares.journeyBeforeBeginWebpaymentNoBasketReference.journeySpecificData
        )
      )

      val doc = Jsoup.parse(contentAsString(systemUnderTest.renderPage(fakeGetRequest))).body()
      doc.select("a.govuk-link").asScala.toList.size shouldBe 3
      doc.select("#summary").text() shouldBe empty
    }
  }

}
