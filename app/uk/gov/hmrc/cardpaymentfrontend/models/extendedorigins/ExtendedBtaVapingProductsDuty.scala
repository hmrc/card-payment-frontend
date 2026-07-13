/*
 * Copyright 2026 HM Revenue & Customs
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

import payapi.cardpaymentjourney.model.journey.{JourneySpecificData, JsdBtaVapingProductsDuty}
import play.api.mvc.AnyContent
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.PaymentMethod.{Bacs, Card, OpenBanking, VariableDirectDebit}
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking.{BtaVapingProductsDutySessionData, OriginSpecificSessionData}
import uk.gov.hmrc.cardpaymentfrontend.models.{CheckYourAnswersRow, PaymentMethod}

object ExtendedBtaVapingProductsDuty extends ExtendedOrigin {
  override val serviceNameMessageKey: String = "service-name.BtaVapingProductsDuty"
  override val taxNameMessageKey: String     = "payment-complete.tax-name.BtaVapingProductsDuty"

  def cardFeesPagePaymentMethods: Set[PaymentMethod] = Set(OpenBanking, Card, VariableDirectDebit)

  def paymentMethods(): Set[PaymentMethod] = Set(Card, OpenBanking, Bacs, VariableDirectDebit)

  override def checkYourAnswersReferenceRow(journeyRequest: JourneyRequest[AnyContent])(payFrontendBaseUrl: String): Option[CheckYourAnswersRow] = None

  override def openBankingOriginSpecificSessionData: JourneySpecificData => Option[OriginSpecificSessionData] = {
    case j: JsdBtaVapingProductsDuty =>
      Some(BtaVapingProductsDutySessionData(j.vapingDutyReference, j.defaultAmountInPence, None))
    case _                           => throw new RuntimeException("Incorrect origin found")
  }

  override def emailTaxTypeMessageKey: String  = "email.tax-name.BtaVapingProductsDuty"
  override def surveyAuditName: String         = "vaping-products-duty"
  override def surveyReturnHref: String        = "https://www.gov.uk/government/organisations/hm-revenue-customs"
  override def surveyReturnMessageKey: String  = "payments-survey.other.return-message"
  override def surveyIsWelshSupported: Boolean = false
  override def surveyBannerTitle: String       = serviceNameMessageKey
}
