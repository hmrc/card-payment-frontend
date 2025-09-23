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

package uk.gov.hmrc.cardpaymentfrontend.functionalbrowserspecs

import org.openqa.selenium
import uk.gov.hmrc.cardpaymentfrontend.models.cardpayment.CardPaymentInitiatePaymentResponse
import uk.gov.hmrc.cardpaymentfrontend.testsupport.BrowserSpec
import uk.gov.hmrc.cardpaymentfrontend.testsupport.stubs.{CardPaymentStub, PayApiStub}
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.TestJourneys

class CheckYourDetailsJavascriptSpec extends BrowserSpec {

  "the check your answers page should have a script that sets the isMobile boolean based on the size of the screen" - {

    "as true (i.e. a mobile device) when screen size is not (window.innerWidth > 599 && window.innerHeight > 799)" in {
      test(
        windowWidth      = 598,
        windowHeight     = 798,
        expectedIsMobile = true
      )
    }

    "as false (i.e. a desktop) when screen size is (window.innerWidth > 599 && window.innerHeight > 799)" in {
      // hint: due to peculiarities with the html unit driver, the width needs to be more due to 'innerWidth' being subset of window width. Likewise with height.
      test(
        windowWidth      = 1000,
        windowHeight     = 1200,
        expectedIsMobile = false
      )
    }
  }

  def test(windowWidth: Int, windowHeight: Int, expectedIsMobile: Boolean): Unit = {
    // -- GIVEN
    // stub setup
    PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
    CardPaymentStub.InitiatePayment.stubForInitiatePayment2xx(CardPaymentInitiatePaymentResponse(s"http://localhost:${port.toString}/this-would-be-iframe", "sometransactionref"))
    //preamble to get the test setup and on the right page
    goTo(s"http://localhost:${port.toString}/pay-by-card/address")
    driver.manage().window().setSize(new selenium.Dimension(windowWidth, windowHeight)) // the window size needs to be higher
    setTextValue("line1", "line1")
    setTextValue("postcode", "AA11AA")
    setSelectValue("country", "GBR")
    clickById("submit")

    // -- WHEN
    //test
    currentUrl shouldBe s"http://localhost:${port.toString}/pay-by-card/check-your-details"
    pageTitle shouldBe "Check your details - Pay your Self Assessment - GOV.UK"
    clickOn("submit")
    val expectedChallengeWindowSizeInUrl = if (expectedIsMobile) "%3FchallengeWindowSize%3DWINDOW_SIZE_600_400" else "%3FchallengeWindowSize%3DFULL_SCREEN"
    currentUrl shouldBe s"http://localhost:${port.toString}/pay-by-card/card-details?iframeUrl=http%3A%2F%2Flocalhost%3A${port.toString}%2Fthis-would-be-iframe$expectedChallengeWindowSizeInUrl"

    // -- THEN
    // The assertion on value sent to Barclaycard.
    // ignoring the redirectUrl in json as the url gets encrypted, painful/overkill to make work, tested elsewhere.
    CardPaymentStub.InitiatePayment.verifyInitiatePayment(
      s"""{
        |  "clientId" : "SAEE",
        |  "purchaseDescription" : "1234567895K",
        |  "purchaseAmount" : 1234,
        |  "billingAddress" : {
        |    "line1" : "line1",
        |    "postCode" : "AA11AA",
        |    "countryCode" : "GBR"
        |  },
        |  "transactionNumber" : "00001999999999",
        |  "isMobile" : ${expectedIsMobile.toString}
        |}""".stripMargin
    )
  }

}
