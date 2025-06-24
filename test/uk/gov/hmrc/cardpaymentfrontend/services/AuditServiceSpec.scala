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

package uk.gov.hmrc.cardpaymentfrontend.services

import play.api.libs.json.{JsObject, Json}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.Address
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec
import uk.gov.hmrc.cardpaymentfrontend.testsupport.TestOps.FakeRequestOps
import uk.gov.hmrc.cardpaymentfrontend.testsupport.stubs.AuditConnectorStub
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.TestJourneys
import uk.gov.hmrc.http.HeaderCarrier

class AuditServiceSpec extends ItSpec {

  override protected lazy val configOverrides: Map[String, Any] = Map[String, Any](
    "auditing.enabled" -> true
  )

  val systemUnderTest: AuditService = app.injector.instanceOf[AuditService]

  "AuditService" - {
    "auditPaymentAttempt should trigger an audit event for paymentAttempt" in {
      val testAddress: Address = Address(line1    = "made up street", line2 = Some("made up line 2"), postcode = "AA11AA", country = "GBR", city = Some("made up city"), county = Some("East sussex"))
      val journeyRequest: JourneyRequest[AnyContentAsEmpty.type] = new JourneyRequest(TestJourneys.PfSa.journeyBeforeBeginWebPayment, FakeRequest().withEmailInSession(TestJourneys.PfSa.journeyBeforeBeginWebPayment._id))
      systemUnderTest.auditPaymentAttempt(testAddress, "SAEE", "some-transaction-reference")(journeyRequest, HeaderCarrier())
      eventually {
        AuditConnectorStub.verifyEventAudited(
          "paymentAttempt",
          Json.parse(
            """
              |{
              | "address": {
              |   "line1" : "made up street",
              |   "line2" : "made up line 2",
              |   "city" : "made up city",
              |   "county" : "East sussex",
              |   "postcode" : "AA11AA",
              |   "country" : "GBR"
              | },
              | "emailAddress": "blah@blah.com",
              | "loggedIn": false,
              | "merchantCode": "SAEE",
              | "paymentOrigin": "PfSa",
              | "paymentReference": "1234567895K",
              | "paymentTaxType": "selfAssessment",
              | "paymentTotal": 12.34,
              | "transactionReference": "some-transaction-reference"
              |}""".stripMargin
          ).as[JsObject]
        )
      }
    }
  }

}
