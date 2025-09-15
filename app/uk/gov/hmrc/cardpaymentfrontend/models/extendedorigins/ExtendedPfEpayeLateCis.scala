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

import payapi.cardpaymentjourney.model.journey.{JourneySpecificData, JsdPfEpayeLateCis}
import play.api.mvc.AnyContent
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.PaymentMethod.{Card, OneOffDirectDebit, OpenBanking}
import uk.gov.hmrc.cardpaymentfrontend.models.{CheckYourAnswersRow, PaymentMethod}
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking.{OriginSpecificSessionData, PfEpayeLateCisSessionData}

object ExtendedPfEpayeLateCis extends ExtendedOrigin {
  override def serviceNameMessageKey: String = "service-name.PfEpayeLateCis"

  override def taxNameMessageKey: String = "payment-complete.tax-name.PfEpayeLateCis"

  override def cardFeesPagePaymentMethods: Set[PaymentMethod] = Set(OpenBanking, OneOffDirectDebit)

  override def paymentMethods(): Set[PaymentMethod] = Set(OneOffDirectDebit, Card, OpenBanking)

  override def checkYourAnswersReferenceRow(journeyRequest: JourneyRequest[AnyContent])
    (payFrontendBaseUrl: String): Option[CheckYourAnswersRow] =
    Some(CheckYourAnswersRow(
      titleMessageKey = "check-your-details.PfEpayeLateCis.reference",
      value           = Seq(journeyRequest.journey.referenceValue),
      changeLink      = None
    ))

  override def openBankingOriginSpecificSessionData: JourneySpecificData => Option[OriginSpecificSessionData] = {
    case j: JsdPfEpayeLateCis => j.prn.map(PfEpayeLateCisSessionData(_))
    case _                    => throw new RuntimeException("Incorrect origin found")
  }

  override def emailTaxTypeMessageKey: String = "email.tax-name.PfEpayeLateCis"

  override def surveyAuditName: String = "paye-late-cis"

  override def surveyReturnHref: String = "https://www.gov.uk/government/organisations/hm-revenue-customs"

  override def surveyReturnMessageKey: String = "payments-survey.other.return-message"

  override def surveyIsWelshSupported: Boolean = true

  override def surveyBannerTitle: String = serviceNameMessageKey
}
