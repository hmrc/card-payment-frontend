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

package uk.gov.hmrc.cardpaymentfrontend.models.extendedorigins

import payapi.cardpaymentjourney.model.journey.{JourneySpecificData, JsdPtaP800}
import payapi.corcommon.model.p800.P800ChargeRef
import play.api.i18n.Lang
import play.api.mvc.AnyContent
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking.OriginSpecificSessionData
import uk.gov.hmrc.cardpaymentfrontend.models.{CheckYourAnswersRow, PaymentMethod}

object ExtendedPtaP800 extends ExtendedOrigin {
  override val serviceNameMessageKey: String = "service-name.PtaP800"
  override val taxNameMessageKey: String = "payment-complete.tax-name.PtaP800"

  def cardFeesPagePaymentMethods: Set[PaymentMethod] = Set()
  def paymentMethods(): Set[PaymentMethod] = Set()

  override def checkYourAnswersReferenceRow(journeyRequest: JourneyRequest[AnyContent])(payFrontendBaseUrl: String): Option[CheckYourAnswersRow] = {
    Some(CheckYourAnswersRow(
      titleMessageKey = "check-your-details.PtaP800.reference",
      value           = Seq(journeyRequest.journey.referenceValue),
      changeLink      = None
    ))
  }

  override def checkYourAnswersAdditionalReferenceRow(journeyRequest: JourneyRequest[AnyContent])(payFrontendBaseUrl: String)(implicit lang: Lang): Option[CheckYourAnswersRow] = {
    additionalReference(journeyRequest.journey.journeySpecificData).map { p800ChargeRef =>
      CheckYourAnswersRow(
        titleMessageKey = "check-your-details.PfP800.charge-reference",
        value           = Seq(p800ChargeRef.canonicalizedValue),
        changeLink      = None
      )
    }
  }

  private def additionalReference: JourneySpecificData => Option[P800ChargeRef] = {
    case j: JsdPtaP800 => j.p800ChargeRef
    case _             => throw new RuntimeException("Incorrect origin found")
  }

  override def openBankingOriginSpecificSessionData: JourneySpecificData => Option[OriginSpecificSessionData] = _ => None
  override def surveyAuditName: String = "p800-or-pa302"
  override def surveyReturnHref: String = "https://www.gov.uk/government/organisations/hm-revenue-customs"
  override def surveyReturnMessageKey: String = "payments-survey.other.return-message"
  override def surveyIsWelshSupported: Boolean = true
  override def surveyBannerTitle: String = serviceNameMessageKey
  override def emailTaxTypeMessageKey: String = "email.tax-name.PfP800"

}
