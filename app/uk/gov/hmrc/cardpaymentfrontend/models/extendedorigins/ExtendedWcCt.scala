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

package uk.gov.hmrc.cardpaymentfrontend.models.extendedorigins

import payapi.cardpaymentjourney.model.journey.{JourneySpecificData, JsdWcCt}
import play.api.mvc.AnyContent
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.PaymentMethod.{Bacs, Card, OpenBanking}
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking.{OriginSpecificSessionData, WcCtSessionData}
import uk.gov.hmrc.cardpaymentfrontend.models.{CheckYourAnswersRow, PaymentMethod}

object ExtendedWcCt extends ExtendedOrigin {
  override val serviceNameMessageKey: String = "service-name.WcCt"
  override val taxNameMessageKey: String = "payment-complete.tax-name.WcCt"

  def cardFeesPagePaymentMethods: Set[PaymentMethod] = Set(OpenBanking)

  def paymentMethods(): Set[PaymentMethod] = Set(Card, OpenBanking, Bacs)

  override def reference(request: JourneyRequest[AnyContent]): String = {
    request.journey.journeySpecificData match {
      case j: JsdWcCt => j.ctPayslipReference.toReference.value
      case _          => throw new RuntimeException("Incorrect origin found")
    }
  }

  override def checkYourAnswersReferenceRow(journeyRequest: JourneyRequest[AnyContent])(payFrontendBaseUrl: String): Option[CheckYourAnswersRow] = {
    Some(CheckYourAnswersRow(
      titleMessageKey = "check-your-details.WcCt.reference",
      value           = Seq(reference(journeyRequest)),
      changeLink      = None
    ))
  }

  override def checkYourAnswersAmountSummaryRow(journeyRequest: JourneyRequest[AnyContent])(payFrontendBaseUrl: String): Option[CheckYourAnswersRow] = Some(CheckYourAnswersRow(
    titleMessageKey = "check-your-details.total-to-pay",
    value           = Seq(amount(journeyRequest)),
    changeLink      = None
  ))

  override def openBankingOriginSpecificSessionData: JourneySpecificData => Option[OriginSpecificSessionData] = {
    case j: JsdWcCt => Some(WcCtSessionData(j.ctPayslipReference.ctUtr, j.ctPayslipReference.ctPeriod, j.ctPayslipReference.ctChargeType))
    case _          => throw new RuntimeException("Incorrect origin found")
  }

  override def emailTaxTypeMessageKey: String = "email.tax-name.WcCt"

  override def surveyAuditName: String = "corporation-tax"
  override def surveyReturnHref: String = "https://www.gov.uk/government/organisations/hm-revenue-customs"
  override def surveyReturnMessageKey: String = "payments-survey.other.return-message"
  override def surveyIsWelshSupported: Boolean = true
  override def surveyBannerTitle: String = serviceNameMessageKey
}
