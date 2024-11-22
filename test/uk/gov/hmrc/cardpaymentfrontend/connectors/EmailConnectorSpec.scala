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

  "PayApiConnector" - {

    "sendEmail" - {

      val emailRequest = EmailRequest(
        to = List("test@email.com"),
        templateId = "payment_successful",
        parameters = EmailParameters("Self Assessment", "ending with 2564K", "transaction-reference", "1,000", None, None),
        force = false
      )

      "should return a 200 when endpoint is hit" in {
        EmailStub.stubForSendEmail(emailRequest)
        systemUnderTest.sendEmail(emailRequest)(HeaderCarrier()).futureValue
        EmailStub.verifyEmailWasSent(jsonBody)
      }

    }

  }

}
