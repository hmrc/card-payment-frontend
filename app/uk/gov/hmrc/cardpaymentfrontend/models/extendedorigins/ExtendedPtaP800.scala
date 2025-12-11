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
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.PaymentMethod.{Bacs, Card, OpenBanking}
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking.{OriginSpecificSessionData, PtaP800SessionData}
import uk.gov.hmrc.cardpaymentfrontend.models.{CheckYourAnswersRow, PaymentMethod}

object ExtendedPtaP800 extends ExtendedOrigin {
  override val serviceNameMessageKey: String = "service-name.PtaP800"
  override val taxNameMessageKey: String = "payment-complete.tax-name.PtaP800"

  def cardFeesPagePaymentMethods: Set[PaymentMethod] = Set(OpenBanking)
  def paymentMethods(): Set[PaymentMethod] = Set(Card, OpenBanking, Bacs)

  override def checkYourAnswersReferenceRow(journeyRequest: JourneyRequest[AnyContent])(payFrontendBaseUrl: String): Option[CheckYourAnswersRow] = {
    Some(CheckYourAnswersRow(
      titleMessageKey = "check-your-details.PtaP800.reference",
      value           = Seq(journeyRequest.journey.referenceValue),
      changeLink      = None
    ))
  }

  override def checkYourAnswersAdditionalReferenceRow(journeyRequest: JourneyRequest[AnyContent])(payFrontendBaseUrl: String)(implicit messages: Messages): Option[Seq[CheckYourAnswersRow]] = {
    journeyRequest.journey.journeySpecificData match {
      case JsdPtaP800(_, maybeP800ChargeRef, taxYear, _) =>
        val chargeRefRow: Seq[CheckYourAnswersRow] = maybeP800ChargeRef.fold(Seq.empty[CheckYourAnswersRow]) { p800ChargeRef =>
          Seq(CheckYourAnswersRow(
            titleMessageKey = "check-your-details.PtaP800.charge-reference",
            value           = Seq(p800ChargeRef.canonicalizedValue),
            changeLink      = None
          ))
        }
        // Pta send us tax year value as start of tax year. TaxYear class in pay-api uses endYear as apply argument. Then startYear: Int = endYear - 1.
        // Therefore for correct displaying, we need to adjust the tax year.
        val adjustedTaxYear = taxYear.nextTaxYear
        val taxYearRow = Seq(CheckYourAnswersRow(
          titleMessageKey = "check-your-details.PtaP800.tax-year",
          value           = Seq(messages("check-your-details.PtaP800.tax-year.value", adjustedTaxYear.startYear.toString, adjustedTaxYear.endYear.toString)),
          changeLink      = None
        ))
        Some(chargeRefRow ++ taxYearRow)

      case _ => throw new RuntimeException("Incorrect origin found")
    }
  }

  override def openBankingOriginSpecificSessionData: JourneySpecificData => Option[OriginSpecificSessionData] = {
    case j: JsdPtaP800 => Some(PtaP800SessionData(j.p800Ref, j.p800ChargeRef, Some(j.taxYear)))
    case _             => throw new RuntimeException("Incorrect origin found")
  }
  override def surveyAuditName: String = "p800-or-pa302"
  override def surveyReturnHref: String = "https://www.gov.uk/government/organisations/hm-revenue-customs"
  override def surveyReturnMessageKey: String = "payments-survey.other.return-message"
  override def surveyIsWelshSupported: Boolean = true
  override def surveyBannerTitle: String = serviceNameMessageKey
  override def emailTaxTypeMessageKey: String = "email.tax-name.PfP800"

}
