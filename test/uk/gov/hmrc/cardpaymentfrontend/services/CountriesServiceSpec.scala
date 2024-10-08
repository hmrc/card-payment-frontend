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
      countries.headOption.map(_.code) shouldBe Some("GBR")
      countries.drop(1).headOption.map(_.code) shouldBe Some("AFG")
      countries.lastOption.map(_.code) shouldBe Some("ZWE")
    }

    "fetch country codes from government register for Welsh" in {
      val countries = service.getCountries(fakeGetRequestInWelsh())
      countries.size shouldBe 249
      countries.headOption.map(_.code) shouldBe Some("GBR")
      countries.drop(1).headOption.map(_.code) shouldBe Some("AFG")
      countries.lastOption.map(_.code) shouldBe Some("GRL")
    }

    "fetch country names from government register for English" in {
      val countries = service.getCountries(fakeGetRequest())
      countries.size shouldBe 249
      countries.headOption.map(_.name) shouldBe Some("United Kingdom")
      countries.drop(1).headOption.map(_.name) shouldBe Some("Afghanistan")
      countries.lastOption.map(_.name) shouldBe Some("Zimbabwe")
    }

    "fetch country names from government register for Welsh" in {
      val countries = service.getCountries(fakeGetRequestInWelsh())
      countries.size shouldBe 249
      countries.headOption.map(_.name) shouldBe Some("Y Deyrnas Unedig")
      countries.drop(1).headOption.map(_.name) shouldBe Some("Affganistan")
      countries.lastOption.map(_.name) shouldBe Some("Yr Ynys Las")
    }

  }
}

