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

import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.audit.{AuditDetail, PaymentAttemptAuditDetail, PaymentResultAuditDetail}
import uk.gov.hmrc.cardpaymentfrontend.models.{Address, EmailAddress}
import uk.gov.hmrc.cardpaymentfrontend.requests.RequestSupport
import uk.gov.hmrc.cardpaymentfrontend.session.JourneySessionSupport.{Keys, RequestOps}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions.auditHeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class AuditService @Inject() (auditConnector: AuditConnector)(implicit ec: ExecutionContext) {

  private val auditSource: String = "card-payment-frontend"

  private def audit[A <: AuditDetail: Writes](a: A)(implicit headerCarrier: HeaderCarrier): Unit = {
    val _ = auditConnector.sendExtendedEvent(
      ExtendedDataEvent(
        auditSource = auditSource,
        auditType   = a.auditType,
        eventId     = UUID.randomUUID().toString,
        tags        = headerCarrier.toAuditTags(),
        detail      = Json.toJson(a)
      )
    )
  }

  private def toPaymentAttempt(
      address:              Address,
      merchantCode:         String,
      transactionReference: String
  )(implicit journeyRequest: JourneyRequest[_]): PaymentAttemptAuditDetail = {
    PaymentAttemptAuditDetail(
      address              = address,
      emailAddress         = journeyRequest.readFromSession[EmailAddress](journeyRequest.journeyId, Keys.email),
      loggedIn             = RequestSupport.isLoggedIn,
      merchantCode         = merchantCode,
      paymentOrigin        = journeyRequest.journey.origin,
      paymentReference     = journeyRequest.journey.referenceValue,
      paymentTaxType       = journeyRequest.journey.taxType,
      paymentTotal         = journeyRequest.journey.getAmountInPence.inPounds,
      transactionReference = transactionReference
    )
  }

  def auditPaymentAttempt(
      address:              Address,
      merchantCode:         String,
      transactionReference: String
  )(implicit journeyRequest: JourneyRequest[_], headerCarrier: HeaderCarrier): Unit =
    audit(toPaymentAttempt(address, merchantCode, transactionReference))

  private def toPaymentResult(
      optionalAddress:      Option[Address],
      merchantCode:         String,
      transactionReference: String,
      paymentStatus:        String,
      journeyRequest:       JourneyRequest[_]
  ): PaymentResultAuditDetail = {
    PaymentResultAuditDetail(
      optionalAddress,
      emailAddress = journeyRequest.readFromSession[EmailAddress](journeyRequest.journeyId, Keys.email),
      loggedIn     = RequestSupport.isLoggedIn(journeyRequest),
      merchantCode,
      paymentOrigin    = journeyRequest.journey.origin,
      paymentStatus    = paymentStatus,
      paymentReference = journeyRequest.journey.referenceValue,
      paymentTaxType   = journeyRequest.journey.taxType,
      paymentTotal     = journeyRequest.journey.getAmountInPence.inPounds,
      transactionReference
    )
  }

  def auditPaymentResult(
      merchantCode:         String,
      transactionReference: String,
      paymentStatus:        String
  )(implicit journeyRequest: JourneyRequest[_], headerCarrier: HeaderCarrier): Unit = {

    audit(toPaymentResult(
      journeyRequest.readFromSession[Address](journeyRequest.journeyId, Keys.address),
      merchantCode,
      transactionReference,
      paymentStatus,
      journeyRequest
    ))
  }

}
