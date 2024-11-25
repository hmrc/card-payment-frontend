package uk.gov.hmrc.cardpaymentfrontend.services

import payapi.cardpaymentjourney.model.journey.{Journey, JourneySpecificData}
import uk.gov.hmrc.cardpaymentfrontend.connectors.EmailConnector
import uk.gov.hmrc.cardpaymentfrontend.models.email.{EmailParameters, EmailRequest}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class EmailService @Inject() (emailConnector: EmailConnector) {

  def sendEmail(journey: Journey[JourneySpecificData], isEnglish: Boolean)(implicit headerCarrier: HeaderCarrier): Future[Unit] = {
    val emailRequest = buildEmailRequest(journey, isEnglish)
    emailConnector.sendEmail(emailRequest)

  }

  protected def buildEmailRequest(journey: Journey[JourneySpecificData], isEnglish: Boolean): EmailRequest = {
    val templateId = if(isEnglish) "payment_successful" else "payment_successful_cy"
    val parameters = EmailParameters("Self Assessment", "ending with 2564K", "transaction-reference", "1,000", None, None)

    EmailRequest(
      to = ???,
      templateId = templateId,
      parameters = parameters,
      force = ???
    )
  }

}
