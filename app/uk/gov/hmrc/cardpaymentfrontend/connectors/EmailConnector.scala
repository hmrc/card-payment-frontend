package uk.gov.hmrc.cardpaymentfrontend.connectors

import play.api.libs.json.Json
import uk.gov.hmrc.cardpaymentfrontend.config.AppConfig
import uk.gov.hmrc.cardpaymentfrontend.models.email.EmailRequest
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.HttpReadsInstances._
import java.net.URL
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class EmailConnector @Inject() (appConfig: AppConfig,
                                httpClientV2: HttpClientV2
                               )(implicit executionContext: ExecutionContext) {

  private val emailUrl: URL = url"${appConfig.emailUrl}/hmrc/email"

  def sendEmail(emailRequest: EmailRequest)(implicit headerCarrier: HeaderCarrier) =
    httpClientV2
    .post(emailUrl)
    .withBody(Json.toJson(emailRequest))
    .execute[Unit]

}
