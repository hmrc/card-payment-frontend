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

import payapi.cardpaymentjourney.model.journey.{JourneySpecificData, JsdVcVatOther}
import payapi.corcommon.model.taxes.vat.VatChargeReference
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.PaymentMethod.*
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking.{OriginSpecificSessionData, VcVatOtherSessionData}
import uk.gov.hmrc.cardpaymentfrontend.models.{CheckYourAnswersRow, PaymentMethod}

object ExtendedVcVatOther extends ExtendedOrigin {
  override val serviceNameMessageKey: String         = "service-name.VcVatOther"
  override val taxNameMessageKey: String             = "payment-complete.tax-name.VcVatOther"
  def cardFeesPagePaymentMethods: Set[PaymentMethod] = Set(OpenBanking)
  def paymentMethods(): Set[PaymentMethod]           = Set(Card, OpenBanking, Bacs)

  override def checkYourAnswersReferenceRow(journeyRequest: JourneyRequest[AnyContent])(payFrontendBaseUrl: String): Option[CheckYourAnswersRow] = {
    Some(
      CheckYourAnswersRow(
        titleMessageKey = "check-your-details.VcVatOther.reference",
        value = Seq(journeyRequest.journey.referenceValue),
        changeLink = None
      )
    )
  }

  private def additionalReference: JourneySpecificData => Option[VatChargeReference] = {
    case j: JsdVcVatOther => Some(j.chargeReference)
    case _                => throw new RuntimeException("Incorrect origin found")
  }

  override def checkYourAnswersAdditionalReferenceRow(
    journeyRequest: JourneyRequest[AnyContent]
  )(payFrontendBaseUrl: String)(implicit messages: Messages): Option[Seq[CheckYourAnswersRow]] = {
    additionalReference(journeyRequest.journey.journeySpecificData).map { chargeReference =>
      Seq(
        CheckYourAnswersRow(
          titleMessageKey = "check-your-details.VcVatOther.charge-reference",
          value = Seq(chargeReference.reference),
          changeLink = None
        )
      )
    }
  }

  override def openBankingOriginSpecificSessionData: JourneySpecificData => Option[OriginSpecificSessionData] = {
    case j: JsdVcVatOther => Some(VcVatOtherSessionData(j.vrn, j.chargeReference))
    case _                => throw new RuntimeException("Incorrect origin found")
  }

  override def emailTaxTypeMessageKey: String = "email.tax-name.VcVatOther"

  override def surveyAuditName: String         = "vat"
  override def surveyReturnHref: String        = "/business-account"
  override def surveyReturnMessageKey: String  = "payments-survey.bta.return-message"
  override def surveyIsWelshSupported: Boolean = true
  override def surveyBannerTitle: String       = serviceNameMessageKey
}
