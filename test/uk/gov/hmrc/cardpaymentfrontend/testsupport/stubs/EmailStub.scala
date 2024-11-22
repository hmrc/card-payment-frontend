package uk.gov.hmrc.cardpaymentfrontend.testsupport.stubs

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalToJson, post, postRequestedFor, stubFor, urlEqualTo, urlPathEqualTo, verify}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.cardpaymentfrontend.models.email.EmailRequest

object EmailStub {

  private val path: String = s"/hmrc/email"

  def verifyEmailWasNotSend(): Unit = verify(0, postRequestedFor(urlEqualTo(path)))

  def verifyEmailWasSent(expectedRequestBody: JsValue): Unit =
    verify(1, postRequestedFor(urlEqualTo(path)).withRequestBody(equalToJson(expectedRequestBody.toString())))


    def stubForSendEmail(emailRequest: EmailRequest): StubMapping = stubFor(
      post(urlPathEqualTo(path)).willReturn(
        aResponse()
          .withStatus(Status.ACCEPTED)
          .withBody(Json.prettyPrint(Json.toJson(emailRequest)))
      )
    )

}
