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

import payapi.cardpaymentjourney.model.journey.{JourneySpecificData, JsdPfEpayeP11d}
import play.api.mvc.{AnyContent, Call}
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.PaymentMethod.{Card, OneOffDirectDebit, OpenBanking}
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking.{OriginSpecificSessionData, PfEpayeP11dSessionData}
import uk.gov.hmrc.cardpaymentfrontend.models.{CheckYourAnswersPeriodRow, CheckYourAnswersRow, Link, PaymentMethod}

object ExtendedPfEpayeP11d extends ExtendedOrigin {

  override def serviceNameMessageKey: String = "service-name.PfEpayeP11d"

  override def taxNameMessageKey: String = "payment-complete.tax-name.PfEpayeP11d"

  override def cardFeesPagePaymentMethods: Set[PaymentMethod] = Set(OneOffDirectDebit, OpenBanking)

  override def paymentMethods(): Set[PaymentMethod] = Set(OneOffDirectDebit, OpenBanking, Card)

  override def checkYourAnswersReferenceRow(journeyRequest: JourneyRequest[AnyContent])
    (payFrontendBaseUrl: String): Option[CheckYourAnswersRow] =
    Some(CheckYourAnswersRow(
      titleMessageKey = "check-your-details.PfEpayeP11d.reference",
      value           = Seq(journeyRequest.journey.referenceValue),
      changeLink      = Some(Link(
        href       = Call("GET", changeReferenceUrl(payFrontendBaseUrl)),
        linkId     = "check-your-details-reference-change-link",
        messageKey = "check-your-details.change"
      ))
    ))

  override def checkYourAnswersAdditionalReferenceRow(journeyRequest: JourneyRequest[AnyContent])
    (payFrontendBaseUrl: String): Option[CheckYourAnswersPeriodRow] =
    Some(CheckYourAnswersPeriodRow(
      titleMessageKey = "check-your-details.PfEpayeP11d.tax-year",
      value           = Seq(journeyRequest.journey.journeySpecificData.asInstanceOf[JsdPfEpayeP11d].period),
      changeLink      = Some(Link(
        href       = Call("GET", s"$payFrontendBaseUrl/change-tax-year?fromCardPayment=true"),
        linkId     = "check-your-details-period-change-link",
        messageKey = "check-your-details.change"
      ))
    ))

  override def openBankingOriginSpecificSessionData: JourneySpecificData => Option[OriginSpecificSessionData] = {
    case j: JsdPfEpayeP11d => j.accountsOfficeReference.flatMap { accountsOfficeReference =>
      j.period.map(PfEpayeP11dSessionData(accountsOfficeReference, _))
    }
    case _ => throw new RuntimeException("Incorrect origin found")
  }

  override def emailTaxTypeMessageKey: String = "email.tax-name.PfEpayeP11d"

  override def surveyAuditName: String = "paye-p11d"

  override def surveyReturnHref: String = "https://www.gov.uk/government/organisations/hm-revenue-customs"

  override def surveyReturnMessageKey: String = "payments-survey.other.return-message"

  override def surveyIsWelshSupported: Boolean = true

  override def surveyBannerTitle: String = serviceNameMessageKey
}
