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

import payapi.cardpaymentjourney.model.journey.{JourneySpecificData, JsdNiEuVatOss}
import payapi.corcommon.model.times.period.CalendarQuarterlyPeriod
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.PaymentMethod._
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking.{NiEuVatOssSessionData, OriginSpecificSessionData}
import uk.gov.hmrc.cardpaymentfrontend.models.{CheckYourAnswersRow, PaymentMethod}
import uk.gov.hmrc.cardpaymentfrontend.util.Period.displayCalendarQuarterAndYear

object ExtendedNiEuVatOss extends ExtendedOrigin {
  override val serviceNameMessageKey: String = "service-name.NiEuVatOss"
  override val taxNameMessageKey: String = "payment-complete.tax-name.NiEuVatOss"

  def cardFeesPagePaymentMethods: Set[PaymentMethod] = Set(OpenBanking)
  def paymentMethods(): Set[PaymentMethod] = Set(Card, OpenBanking, Bacs)

  override def checkYourAnswersReferenceRow(journeyRequest: JourneyRequest[AnyContent])(payFrontendBaseUrl: String): Option[CheckYourAnswersRow] = {
    journeyRequest.journey.journeySpecificData.reference.map { vrn =>
      CheckYourAnswersRow(
        titleMessageKey = "check-your-details.NiEuVatOss.reference",
        value           = Seq(vrn.value),
        changeLink      = None
      )
    }
  }

  override def checkYourAnswersAdditionalReferenceRow(journeyRequest: JourneyRequest[AnyContent])(payFrontendBaseUrl: String)(implicit messages: Messages): Option[Seq[CheckYourAnswersRow]] = {
    val period: CalendarQuarterlyPeriod = journeyRequest.journey.journeySpecificData match {
      case jsd: JsdNiEuVatOss => jsd.period
      case _                  => throw new RuntimeException("Incorrect origin found")
    }
    Some(Seq(CheckYourAnswersRow(
      titleMessageKey = "check-your-details.NiEuVatOss.tax-year",
      value           = Seq(displayCalendarQuarterAndYear(period)),
      changeLink      = None
    )))
  }

  override def openBankingOriginSpecificSessionData: JourneySpecificData => Option[OriginSpecificSessionData] = {
    case j: JsdNiEuVatOss => Some(NiEuVatOssSessionData(j.vrn, j.period))
    case _                => throw new RuntimeException("Incorrect origin found")
  }

  override def surveyAuditName: String = "vat"
  override def surveyReturnHref: String = "https://www.gov.uk/government/organisations/hm-revenue-customs"
  override def surveyReturnMessageKey: String = "payments-survey.other.return-message"
  override def surveyIsWelshSupported: Boolean = false
  override def surveyBannerTitle: String = serviceNameMessageKey

  override def emailTaxTypeMessageKey: String = "email.tax-name.NiEuVatOss"

}
