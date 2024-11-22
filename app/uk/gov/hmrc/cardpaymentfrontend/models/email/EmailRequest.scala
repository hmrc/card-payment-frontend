package uk.gov.hmrc.cardpaymentfrontend.models.email

import play.api.libs.json.{Format, Json}

final case class EmailRequest(
                               to: List[String],
                               templateId: String,
                               parameters: EmailParameters,
                               force: Boolean
                             ) {

}

object EmailRequest {
  implicit val format: Format[EmailRequest] = Json.format[EmailRequest]
}
