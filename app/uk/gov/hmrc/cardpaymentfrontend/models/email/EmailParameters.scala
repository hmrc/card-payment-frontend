package uk.gov.hmrc.cardpaymentfrontend.models.email

import play.api.libs.json.{Format, Json}

final case class EmailParameters(
  taxType: String,
  taxReference: String,
  paymentReference: String,
  amountPaid: String,
  commission: Option[String],
  totalPaid: Option[String]
) {

}

object EmailParameters {
  implicit val format: Format[EmailParameters] = Json.format[EmailParameters]
}
