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

package uk.gov.hmrc.cardpaymentfrontend.models.cardpayment

import play.api.libs.json._
import uk.gov.hmrc.cardpaymentfrontend.models.cardpayment.CardPaymentFinishPaymentResponses.CardPaymentFinishPaymentResponse
import uk.gov.hmrc.cardpaymentfrontend.models.payapi.{FailWebPaymentRequest, FinishedWebPaymentRequest, SucceedWebPaymentRequest}
import uk.gov.hmrc.cardpaymentfrontend.util.SafeEquals.EqualsOps

import java.time.LocalDateTime

object CardPaymentFinishPaymentResponses {

  implicit val format: Format[CardPaymentFinishPaymentResponse] = {
    val reads: Reads[CardPaymentFinishPaymentResponse] = Reads {
      case JsString(s) if s === "Successful" => JsSuccess(Successful)
      case JsString(s) if s === "Failed"     => JsSuccess(Failed)
      case JsString(s) if s === "Cancelled"  => JsSuccess(Cancelled)
      case _                                 => JsError("couldn't parse status as CardPaymentFinishPaymentResponse")
    }
    val writes: Writes[CardPaymentFinishPaymentResponse] = Writes(status => JsString(status.toString))

    Format(reads, writes)
  }

  sealed trait CardPaymentFinishPaymentResponse

  case object Successful extends CardPaymentFinishPaymentResponse

  case object Failed extends CardPaymentFinishPaymentResponse

  case object Cancelled extends CardPaymentFinishPaymentResponse

}

final case class AdditionalPaymentInfo(cardCategory: Option[String], commissionInPence: Option[Long], transactionTime: Option[LocalDateTime])

object AdditionalPaymentInfo {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: Format[AdditionalPaymentInfo] = Json.format[AdditionalPaymentInfo]
}

final case class CardPaymentResult(cardPaymentResult: CardPaymentFinishPaymentResponse, additionalPaymentInfo: AdditionalPaymentInfo) {

  //todo this should probably live somewhere else? also we need to write test for it
  def intoUpdateWebPaymentRequest: Option[FinishedWebPaymentRequest] = this.cardPaymentResult match {
    case CardPaymentFinishPaymentResponses.Successful =>
      Some(SucceedWebPaymentRequest(
        additionalPaymentInfo.cardCategory.getOrElse("debit"),
        additionalPaymentInfo.commissionInPence,
        additionalPaymentInfo.transactionTime.getOrElse(LocalDateTime.now()),
      ))
    case CardPaymentFinishPaymentResponses.Failed =>
      Some(FailWebPaymentRequest(
        additionalPaymentInfo.transactionTime.getOrElse(LocalDateTime.now()),
        additionalPaymentInfo.cardCategory.getOrElse("debit") //todo check if can this be anything?
      ))
    case CardPaymentFinishPaymentResponses.Cancelled => None
  }
}

object CardPaymentResult {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: Format[CardPaymentResult] = Json.format[CardPaymentResult]
}
