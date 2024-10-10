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

import play.api.mvc.{AnyContentAsEmpty, Cookie}
import play.api.test.FakeRequest
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec

class CountriesServiceSpec extends ItSpec {

  private val service = app.injector.instanceOf[CountriesService]

  def fakeGetRequest(): FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/address")

  def fakeGetRequestInWelsh(): FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/address").withCookies(Cookie("PLAY_LANG", "cy"))

  "service" - {

    "Return a sequence of country codes in English" in {
      val countries = service.getCountries(fakeGetRequest())
      countries.size shouldBe 249
      countries.headOption.map(_.code) shouldBe Some("GBR")
      countries.drop(1).headOption.map(_.code) shouldBe Some("AFG")
      countries.lastOption.map(_.code) shouldBe Some("ZWE")
    }

    "Return a sequence of country codes in Welsh" in {
      val countries = service.getCountries(fakeGetRequestInWelsh())
      countries.size shouldBe 249
      countries.headOption.map(_.code) shouldBe Some("GBR")
      countries.drop(1).headOption.map(_.code) shouldBe Some("AFG")
      countries.lastOption.map(_.code) shouldBe Some("GRL")
    }

    "Return a sequence of country names in English" in {
      val countries = service.getCountries(fakeGetRequest())
      countries.size shouldBe 249
      countries.headOption.map(_.name) shouldBe Some("United Kingdom")
      countries.drop(1).headOption.map(_.name) shouldBe Some("Afghanistan")
      countries.lastOption.map(_.name) shouldBe Some("Zimbabwe")
    }

    "Return a sequence of country names in Welsh" in {
      val countries = service.getCountries(fakeGetRequestInWelsh())
      countries.size shouldBe 249
      countries.headOption.map(_.name) shouldBe Some("Y Deyrnas Unedig")
      countries.drop(1).headOption.map(_.name) shouldBe Some("Affganistan")
      countries.lastOption.map(_.name) shouldBe Some("Yr Ynys Las")
    }

  }
}

