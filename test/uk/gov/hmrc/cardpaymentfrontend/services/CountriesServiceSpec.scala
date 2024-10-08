/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.cardpaymentfrontend.services

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.mvc.{AnyContentAsFormUrlEncoded, Cookie}
import play.api.test.FakeRequest

class CountriesServiceSpec extends AnyWordSpec with Matchers with ScalaFutures with GuiceOneServerPerSuite {

  private val service = app.injector.instanceOf[CountriesService]

  def fakeGetRequest(formData: (String, String)*): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest("GET", "/address").withFormUrlEncodedBody(formData: _*)

  def fakeGetRequestInWelsh(formData: (String, String)*): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest("GET", "/address").withFormUrlEncodedBody(formData: _*).withCookies(Cookie("PLAY_LANG", "cy"))

  "service" should {

    "fetch country codes from government register for English" in {
      val countries = service.getCountries(fakeGetRequest())
      countries.size shouldBe 249
      countries.head.code shouldBe "GBR"
      countries(1).code shouldBe "AFG"
      countries.last.code shouldBe "ZWE"
    }

    "fetch country codes from government register for Welsh" in {
      val countries = service.getCountries(fakeGetRequestInWelsh())
      countries.size shouldBe 249
      countries.head.code shouldBe "GBR"
      countries(1).code shouldBe "AFG"
      countries.last.code shouldBe "GRL"
    }

    "fetch country names from government register for English" in {
      val countries = service.getCountries(fakeGetRequest())
      countries.size shouldBe 249
      countries.head.name shouldBe "United Kingdom"
      countries(1).name shouldBe "Afghanistan"
      countries.last.name shouldBe "Zimbabwe"
    }

    "fetch country names from government register for Welsh" in {
      val countries = service.getCountries(fakeGetRequestInWelsh())
      countries.size shouldBe 249
      countries.head.name shouldBe "Y Deyrnas Unedig"
      countries(1).name shouldBe "Affganistan"
      countries.last.name shouldBe "Yr Ynys Las"
    }

  }
}

