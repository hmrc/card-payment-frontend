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

package uk.gov.hmrc.cardpaymentfrontend.session

import payapi.corcommon.model.JourneyId
import play.api.libs.json.Json
import play.api.mvc.RequestHeader
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.controllers.AddressController
import uk.gov.hmrc.cardpaymentfrontend.models.Address
import uk.gov.hmrc.cardpaymentfrontend.session.JourneySessionSupport.*
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.TestJourneys

class JourneySessionSupportSpec extends ItSpec {

  val addressController: AddressController = app.injector.instanceOf[AddressController]

  "JourneySessionSupport" - {

    "RequestOps.readFromSession" - {

      // todo probably remove when we remove the old session key backup thing -- add ticket number
      "should be able to read the old session key and return the value when new onlinePaymentService_ prefix is not present" in {
        val journey     = TestJourneys.PfSa.journeyBeforeBeginWebPayment
        val testAddress =
          Address(
            line1 = "line1",
            line2 = Some("line2"),
            city = Some("city"),
            county = Some("county"),
            postcode = Some("AA0AA0"),
            country = "GBR"
          )

        def fakeRequest(journeyId: JourneyId, address: Address) = FakeRequest().withSession(
          journeyId.value -> Json
            .obj(
              "address" -> address
            )
            .toString
        )

        val fakeJourneyRequest      = new JourneyRequest(journey, fakeRequest(journey._id, testAddress))
        val result: Option[Address] = fakeJourneyRequest.readFromSession[Address](fakeJourneyRequest.journey._id, "address")
        result shouldBe Some(testAddress)
      }

      "should be able to read a value from session using the new onlinePaymentsService_ prefix" in {
        val journey     = TestJourneys.PfSa.journeyBeforeBeginWebPayment
        val testAddress =
          Address(
            line1 = "line1",
            line2 = Some("line2"),
            city = Some("city"),
            county = Some("county"),
            postcode = Some("AA0AA0"),
            country = "GBR"
          )

        def fakeRequest(journeyId: JourneyId, address: Address) = FakeRequest().withSession(
          s"onlinePaymentsService_${journeyId.value}" -> Json
            .obj(
              "address" -> address
            )
            .toString
        )

        val fakeJourneyRequest      = new JourneyRequest(journey, fakeRequest(journey._id, testAddress))
        val result: Option[Address] = fakeJourneyRequest.readFromSession[Address](fakeJourneyRequest.journey._id, "address")
        result shouldBe Some(testAddress)
      }
    }

    "ResultOps.placeInSession" - {
      "should place a value in session using the onlinePaymentsService_ prefix" in {
        val journey = TestJourneys.PfSa.journeyBeforeBeginWebPayment

        val testAddress =
          Address(
            line1 = "line1",
            line2 = Some("line2"),
            city = Some("city"),
            county = Some("county"),
            postcode = Some("AA0AA0"),
            country = "GBR"
          )

        implicit val fakeRequest: RequestHeader = FakeRequest()

        val result = Redirect("/next-page").placeInSession(journey._id, Keys.address -> testAddress)

        val sessionData = result.session(fakeRequest).get(s"onlinePaymentsService_${journey._id.value}")
        sessionData shouldBe defined

        val address = (Json.parse(sessionData.value) \ "address").as[Address]
        address shouldBe testAddress
      }

      "should place a value in the session and remove stale journeys from session using the onlinePaymentsService_ prefix" in {
        val journey1 = TestJourneys.PfSa.journeyBeforeBeginWebPayment.copy(_id = JourneyId("journey1"))
        val journey2 = TestJourneys.PfSa.journeyBeforeBeginWebPayment.copy(_id = JourneyId("journey2"))

        val testAddress =
          Address(
            line1 = "line1",
            line2 = Some("line2"),
            city = Some("city"),
            county = Some("county"),
            postcode = Some("AA0AA0"),
            country = "GBR"
          )

        implicit val fakeRequest: RequestHeader =
          FakeRequest().withSession(s"onlinePaymentsService_${journey1._id.value}" -> Json.obj("address" -> "stale address").toString)

        val result = Redirect("/next-page").placeInSession(journey2._id, Keys.address -> testAddress)

        val sessionData = result.session(fakeRequest)

        sessionData.get(s"onlinePaymentsService_${journey1._id.value}") shouldBe None
        sessionData.get(s"onlinePaymentsService_${journey2._id.value}") shouldBe defined
        sessionData.data.keySet should not contain s"onlinePaymentsService_${journey1._id.value}"
        sessionData.data.keySet should contain(s"onlinePaymentsService_${journey2._id.value}")

      }
    }

    "ResultOps.removingJourneyFromSession" - {
      "should remove a journey from the session using the onlinePaymentsService_ prefix" in {
        val journey     = TestJourneys.PfSa.journeyBeforeBeginWebPayment
        val testAddress =
          Address(
            line1 = "line1",
            line2 = Some("line2"),
            city = Some("city"),
            county = Some("county"),
            postcode = Some("AA0AA0"),
            country = "GBR"
          )

        implicit val fakeRequest: RequestHeader =
          FakeRequest().withSession(
            s"onlinePaymentsService_${journey._id.value}" ->
              Json
                .obj(
                  "address" -> testAddress,
                  "email"   -> "pikachu@pokemon.com"
                )
                .toString
          )

        val result = Redirect("/next-page").removingJourneyFromSession(journey._id, Keys.address)

        val sessionData = result.session(fakeRequest).get(s"onlinePaymentsService_${journey._id.value}")
        sessionData shouldBe defined

        val session = result.session(fakeRequest)
        // It's a bit confusing what .removingJourneyFromSession is doing, it looks like it is supposed to remove the key specified from the journey,
        // but it is removing all the keys under the journeyId key, so we are left with an empty Json object
        session.get(s"onlinePaymentsService_${journey._id.value}") shouldBe Some("{ }")
      }

    }
  }
}
