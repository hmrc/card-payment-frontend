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
import play.api.test.FakeRequest
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.Address
import uk.gov.hmrc.cardpaymentfrontend.session.JourneySessionSupport.*
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.TestJourneys

class JourneySessionSupportSpec extends ItSpec {

  "JourneySessionSupport" - {

    "RequestOps.readFromSession" - {

      // todo probably remove when we remove the old session key backup thing -- add ticket number
      "should be able to read the old session key and return the value when new onlinePaymentService_ prefix is not present" in {
        val journey                                             = TestJourneys.PfSa.journeyBeforeBeginWebPayment
        val testAddress                                         =
          Address(line1 = "line1", line2 = Some("line2"), city = Some("city"), county = Some("county"), postcode = Some("AA0AA0"), country = "GBR")
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
          Address(line1 = "line1", line2 = Some("line2"), city = Some("city"), county = Some("county"), postcode = Some("AA0AA0"), country = "GBR")

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

    "add" - {
      // todo Mike - add a test for the new onlinePaymentsService_ prefix
      "should add a new value to session under key with prefix blah" in {}
      "should add a new value to session under key with prefix blah and delete any old journey keys" in {}
    }

    "remove" - {
      // todo Mike - add a test for the new onlinePaymentsService_ prefix
      "should remove a value from session under key with prefix blah" in {}
      "should remove a value from session under key with prefix blah and delete any old journey keys" in {}
    }

    "removeStaleJourneysFromSession" - {

      "should remove stale journeys from session" in {
        val journey1 = TestJourneys.PfSa.journeyBeforeBeginWebPayment.copy(_id = JourneyId("journey1"))
        val journey2 = TestJourneys.PfSa.journeyBeforeBeginWebPayment.copy(_id = JourneyId("journey2"))

        val fakeRequest = FakeRequest().withSession(
          s"onlinePaymentsService_${journey1._id.value}" -> Json.obj("key1" -> "value1").toString,
          s"onlinePaymentsService_${journey2._id.value}" -> Json.obj("key2" -> "value2").toString
        )

        val updatedSession = JourneySessionSupport.removeStaleJourneysFromSession(fakeRequest.session, journey2._id)

        updatedSession.data should contain(s"onlinePaymentsService_${journey1._id.value}" -> Json.obj("key1" -> "value1").toString)
        updatedSession.data should not contain (s"onlinePaymentsService_${journey2._id.value}" -> Json.obj("key2" -> "value2").toString)
      }
    }
  }
}
