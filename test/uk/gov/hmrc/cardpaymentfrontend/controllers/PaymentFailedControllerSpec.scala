package uk.gov.hmrc.cardpaymentfrontend.controllers

import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.status
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec
import play.api.test.Helpers._

class PaymentFailedControllerSpec extends ItSpec {

  private val systemUnderTest: PaymentFailedController = app.injector.instanceOf[PaymentFailedController]

  "PaymentFailedController" - {

    "GET /payment-failed" - {

      val fakeGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/payment-failed")
      //      val fakeGetRequestInWelsh: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/email-address").withCookies(Cookie("PLAY_LANG", "cy"))

      "should return 200 OK" in {
        val result = systemUnderTest.renderPage(fakeGetRequest)
        status(result) shouldBe Status.OK
      }

    }

  }

}
