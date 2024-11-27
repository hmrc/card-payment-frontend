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

package uk.gov.hmrc.cardpaymentfrontend.connectors

import play.api.libs.json.Json
import uk.gov.hmrc.cardpaymentfrontend.models.email.{EmailParameters, EmailRequest}
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec
import uk.gov.hmrc.cardpaymentfrontend.testsupport.stubs.EmailStub
import uk.gov.hmrc.http.HeaderCarrier

class EmailConnectorSpec extends ItSpec {

  val systemUnderTest: EmailConnector = app.injector.instanceOf[EmailConnector]

  val jsonBody = Json.parse(

    """
        {
          "to" : [ "test@email.com" ],
          "templateId" : "payment_successful",
          "parameters" : {
            "taxType" : "Self Assessment",
            "taxReference" : "ending with 2564K",
            "paymentReference" : "transaction-reference",
            "amountPaid" : "1,000"
          },
          "force" : false
        }
        """
  )

  "EmailConnector" - {

    ".sendEmail" - {

      val emailRequest = EmailRequest(
        to         = List("test@email.com"),
        templateId = "payment_successful",
        parameters = EmailParameters("Self Assessment", "ending with 2564K", "transaction-reference", "1,000", None, None),
        force      = false
      )

      "should return a 202 ACCEPTED when endpoint is hit" in {
        EmailStub.stubForSendEmail(emailRequest)
        systemUnderTest.sendEmail(emailRequest)(HeaderCarrier()).futureValue
        EmailStub.verifyEmailWasSent(jsonBody)
      }

    }

  }

}
