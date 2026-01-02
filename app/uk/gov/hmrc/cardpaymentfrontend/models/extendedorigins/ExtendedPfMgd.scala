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

import payapi.cardpaymentjourney.model.journey.{JourneySpecificData, JsdPfMgd}
import play.api.mvc.{AnyContent, Call}
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.PaymentMethod._
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking.{OriginSpecificSessionData, PfMgdSessionData}
import uk.gov.hmrc.cardpaymentfrontend.models.{CheckYourAnswersRow, Link, PaymentMethod}

object ExtendedPfMgd extends ExtendedOrigin {
  override val serviceNameMessageKey: String = "service-name.PfMgd"
  override val taxNameMessageKey: String     = "payment-complete.tax-name.PfMgd"

  def cardFeesPagePaymentMethods: Set[PaymentMethod] = Set(Card, OpenBanking, VariableDirectDebit, OneOffDirectDebit)

  def paymentMethods(): Set[PaymentMethod] = Set(Card, OpenBanking, VariableDirectDebit, OneOffDirectDebit, Bacs)

  override def checkYourAnswersReferenceRow(journeyRequest: JourneyRequest[AnyContent])(payFrontendBaseUrl: String): Option[CheckYourAnswersRow] = {
    Some(
      CheckYourAnswersRow(
        titleMessageKey = "check-your-details.PfMgd.reference",
        value = Seq(journeyRequest.journey.referenceValue),
        changeLink = Some(
          Link(
            href = Call("GET", changeReferenceUrl(payFrontendBaseUrl)),
            linkId = "check-your-details-reference-change-link",
            messageKey = "check-your-details.change"
          )
        )
      )
    )
  }

  override def openBankingOriginSpecificSessionData: JourneySpecificData => Option[OriginSpecificSessionData] = {
    case j: JsdPfMgd => j.prn.map(mgdRef => PfMgdSessionData(mgdRef))
    case _           => throw new RuntimeException("Incorrect origin found")
  }

  override def emailTaxTypeMessageKey: String  = "email.tax-name.PfMgd"
  override def surveyAuditName: String         = "machine-games-duty"
  override def surveyReturnHref: String        = "https://www.gov.uk/government/organisations/hm-revenue-customs"
  override def surveyReturnMessageKey: String  = "payments-survey.other.return-message"
  override def surveyIsWelshSupported: Boolean = true
  override def surveyBannerTitle: String       = serviceNameMessageKey

}
