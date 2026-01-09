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

import payapi.cardpaymentjourney.model.journey.{JourneySpecificData, JsdCapitalGainsTax}
import play.api.mvc.AnyContent
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.PaymentMethod.{Card, OpenBanking}
import uk.gov.hmrc.cardpaymentfrontend.models.{CheckYourAnswersRow, PaymentMethod}
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking.{CapitalGainsTaxSessionData, OriginSpecificSessionData}

object ExtendedCapitalGainsTax extends ExtendedOrigin {
  override def serviceNameMessageKey: String = "service-name.CapitalGainsTax"

  override def taxNameMessageKey: String = "payment-complete.tax-name.CapitalGainsTax"

  override def cardFeesPagePaymentMethods: Set[PaymentMethod] = Set(OpenBanking)

  override def paymentMethods(): Set[PaymentMethod] = Set(OpenBanking, Card)

  override def checkYourAnswersReferenceRow(journeyRequest: JourneyRequest[AnyContent])(payFrontendBaseUrl: String): Option[CheckYourAnswersRow] =
    Some(
      CheckYourAnswersRow(
        titleMessageKey = "check-your-details.CapitalGainsTax.reference",
        value = Seq(journeyRequest.journey.referenceValue),
        changeLink = None
      )
    )

  override def openBankingOriginSpecificSessionData: JourneySpecificData => Option[OriginSpecificSessionData] = {
    case j: JsdCapitalGainsTax => Some(CapitalGainsTaxSessionData(j.cgtReference))
    case _                     => throw new RuntimeException("Incorrect origin found")
  }

  override def emailTaxTypeMessageKey: String = "email.tax-name.CapitalGainsTax"

  override def surveyAuditName: String         = "capital-gains-tax"
  override def surveyReturnHref: String        = "https://www.gov.uk/government/organisations/hm-revenue-customs"
  override def surveyReturnMessageKey: String  = "payments-survey.other.return-message"
  override def surveyIsWelshSupported: Boolean = true
  override def surveyBannerTitle: String       = serviceNameMessageKey
}
