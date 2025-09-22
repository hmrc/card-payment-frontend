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

import payapi.cardpaymentjourney.model.journey.{JourneySpecificData, JsdPfEpayeNi}
import play.api.i18n.Messages
import play.api.mvc.{AnyContent, Call}
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.PaymentMethod.{Card, OneOffDirectDebit, OpenBanking, VariableDirectDebit}
import uk.gov.hmrc.cardpaymentfrontend.models.{CheckYourAnswersRow, Link, PaymentMethod}
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking.{OriginSpecificSessionData, PfEpayeNiSessionData}
import uk.gov.hmrc.cardpaymentfrontend.util.Period.humanReadablePeriod

object ExtendedPfEpayeNi extends ExtendedOrigin {

  override def serviceNameMessageKey: String = "service-name.PfEpayeNi"

  override def taxNameMessageKey: String = "payment-complete.tax-name.PfEpayeNi"

  override def cardFeesPagePaymentMethods: Set[PaymentMethod] = Set(OpenBanking, VariableDirectDebit, OneOffDirectDebit)

  override def paymentMethods(): Set[PaymentMethod] = Set(OneOffDirectDebit, VariableDirectDebit, Card, OpenBanking)

  override def checkYourAnswersReferenceRow(journeyRequest: JourneyRequest[AnyContent])
    (payFrontendBaseUrl: String): Option[CheckYourAnswersRow] =
    Some(CheckYourAnswersRow(
      titleMessageKey = "check-your-details.PfEpayeNi.reference",
      value           = Seq(journeyRequest.journey.journeySpecificData.asInstanceOf[JsdPfEpayeNi].accountsOfficeReference.fold("")(_.value)),
      changeLink      = Some(Link(
        href       = Call("GET", changeReferenceUrl(payFrontendBaseUrl)),
        linkId     = "check-your-details-reference-change-link",
        messageKey = "check-your-details.change"
      ))
    ))

  override def checkYourAnswersAdditionalReferenceRow(journeyRequest: JourneyRequest[AnyContent])
    (payFrontendBaseUrl: String)(implicit messages: Messages): Option[Seq[CheckYourAnswersRow]] = {
    journeyRequest.journey.journeySpecificData.asInstanceOf[JsdPfEpayeNi].period.map { period =>
      Seq(CheckYourAnswersRow(
        titleMessageKey = "check-your-details.PfEpayeNi.tax-period",
        value           = Seq(humanReadablePeriod(period)(messages.lang)),
        changeLink      = Some(Link(
          href       = Call("GET", s"$payFrontendBaseUrl/change-employers-paye-period?fromCardPayment=true"),
          linkId     = "check-your-details-period-change-link",
          messageKey = "check-your-details.change"
        ))
      ))
    }
  }

  override def openBankingOriginSpecificSessionData: JourneySpecificData => Option[OriginSpecificSessionData] = {
    case j: JsdPfEpayeNi => j.accountsOfficeReference.flatMap { accountsOfficeReference =>
      j.period.map(PfEpayeNiSessionData(accountsOfficeReference, _))
    }
    case _ => throw new RuntimeException("Incorrect origin found")
  }

  override def emailTaxTypeMessageKey: String = "email.tax-name.PfEpayeNi"

  override def surveyAuditName: String = "epaye"

  override def surveyReturnHref: String = "https://www.gov.uk/government/organisations/hm-revenue-customs"

  override def surveyReturnMessageKey: String = "payments-survey.other.return-message"

  override def surveyIsWelshSupported: Boolean = true

  override def surveyBannerTitle: String = serviceNameMessageKey
}
