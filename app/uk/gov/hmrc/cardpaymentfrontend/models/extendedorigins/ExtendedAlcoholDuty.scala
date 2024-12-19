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

import payapi.cardpaymentjourney.model.journey.{JourneySpecificData, JsdAlcoholDuty}
import payapi.corcommon.model.taxes.ad.AlcoholDutyChargeReference
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.{CheckYourAnswersRow, PaymentMethod}
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking.{AlcoholDutySessionData, OriginSpecificSessionData}
import uk.gov.hmrc.cardpaymentfrontend.models.PaymentMethod.{Bacs, Card, OpenBanking}

object ExtendedAlcoholDuty extends ExtendedOrigin {
  override val serviceNameMessageKey: String = "service-name.AlcoholDuty"
  override val taxNameMessageKey: String = "payment-complete.tax-name.AlcoholDuty"

  def cardFeesPagePaymentMethods: Set[PaymentMethod] = Set(OpenBanking, Card)

  def paymentMethods(): Set[PaymentMethod] = Set(Card, OpenBanking, Bacs)

  //todo delete from ExtendedOrigin trait, we won't use it anymore
  def checkYourAnswersRows(request: JourneyRequest[AnyContent])(implicit messages: Messages): Seq[CheckYourAnswersRow] = Seq.empty

  override def checkYourAnswersReferenceRow(journeyRequest: JourneyRequest[AnyContent]): Option[CheckYourAnswersRow] = {
    Some(CheckYourAnswersRow(
      titleMessageKey = "check-your-details.AlcoholDuty.reference",
      value           = Seq(journeyRequest.journey.referenceValue),
      changeLink      = None
    ))
  }

  private def additionalReference: JourneySpecificData => Option[AlcoholDutyChargeReference] = {
    case j: JsdAlcoholDuty => j.alcoholDutyChargeReference
    case _                 => throw new RuntimeException("Incorrect origin found")
  }

  override def checkYourAnswersAdditionalReferenceRow(journeyRequest: JourneyRequest[AnyContent]): Option[CheckYourAnswersRow] = {
    additionalReference(journeyRequest.journey.journeySpecificData).map{ alcoholDutyChargeReference =>
      CheckYourAnswersRow(
        titleMessageKey = "check-your-details.AlcoholDuty.charge-reference",
        value           = Seq(alcoholDutyChargeReference.canonicalizedValue),
        changeLink      = None
      )
    }
  }

  override def openBankingOriginSpecificSessionData: JourneySpecificData => Option[OriginSpecificSessionData] = {
    case j: JsdAlcoholDuty => Some(AlcoholDutySessionData(j.alcoholDutyReference, j.alcoholDutyChargeReference))
    case _                 => throw new RuntimeException("Incorrect origin found")
  }

  override def emailTaxTypeMessageKey: String = "email.tax-name.AlcoholDuty"

  override def surveyAuditName: String = "alcohol-duty"
  override def surveyReturnHref: String = "https://www.gov.uk/government/organisations/hm-revenue-customs"
  override def surveyReturnMessageKey: String = "payments-survey.other.return-message"
  override def surveyIsWelshSupported: Boolean = true
  override def surveyBannerTitle: String = serviceNameMessageKey
}
