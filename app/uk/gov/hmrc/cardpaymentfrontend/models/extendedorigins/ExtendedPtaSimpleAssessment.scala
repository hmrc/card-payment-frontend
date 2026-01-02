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

import payapi.cardpaymentjourney.model.journey.{JourneySpecificData, JsdPtaSimpleAssessment}
import payapi.corcommon.model.times.period.TaxYear
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.PaymentMethod._
import uk.gov.hmrc.cardpaymentfrontend.models._
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking.{OriginSpecificSessionData, PtaSimpleAssessmentSessionData}

object ExtendedPtaSimpleAssessment extends ExtendedOrigin {
  override val serviceNameMessageKey: String = "service-name.PtaSimpleAssessment"
  override val taxNameMessageKey: String     = "payment-complete.tax-name.PtaSimpleAssessment"

  def cardFeesPagePaymentMethods: Set[PaymentMethod] = Set(OpenBanking)
  def paymentMethods(): Set[PaymentMethod]           = Set(Card, OpenBanking, Bacs)

  override def checkYourAnswersReferenceRow(journeyRequest: JourneyRequest[AnyContent])(payFrontendBaseUrl: String): Option[CheckYourAnswersRow] = {
    Some(
      CheckYourAnswersRow(
        titleMessageKey = "check-your-details.PtaSimpleAssessment.reference",
        value = Seq(journeyRequest.journey.referenceValue),
        changeLink = None
      )
    )
  }

  override def checkYourAnswersAdditionalReferenceRow(
    journeyRequest: JourneyRequest[AnyContent]
  )(payFrontendBaseUrl: String)(implicit messages: Messages): Option[Seq[CheckYourAnswersRow]] = {
    journeyRequest.journey.journeySpecificData match {
      case JsdPtaSimpleAssessment(_, p302ChargeRef, taxYear, _, _) =>
        // Pta send us tax year value as start of tax year. TaxYear class in pay-api uses endYear as apply argument. Then startYear: Int = endYear - 1.
        // Therefore for correct displaying, we need to adjust the tax year.
        val adjustedTaxYear: TaxYear = taxYear.nextTaxYear
        Some(
          Seq(
            CheckYourAnswersRow(
              titleMessageKey = "check-your-details.PtaSimpleAssessment.charge-reference",
              value = Seq(p302ChargeRef.canonicalizedValue),
              changeLink = None
            ),
            CheckYourAnswersRow(
              titleMessageKey = "check-your-details.PtaSimpleAssessment.tax-year",
              value =
                Seq(messages("check-your-details.PtaSimpleAssessment.tax-year.value", adjustedTaxYear.startYear.toString, adjustedTaxYear.endYear.toString)),
              changeLink = None
            )
          )
        )
      case _                                                       => throw new RuntimeException("Incorrect origin found")
    }

  }

  override def openBankingOriginSpecificSessionData: JourneySpecificData => Option[OriginSpecificSessionData] = {
    case j: JsdPtaSimpleAssessment => Some(PtaSimpleAssessmentSessionData(j.p302Ref, j.p302ChargeRef))
    case _                         => throw new RuntimeException("Incorrect origin found")
  }

  override def surveyAuditName: String         = "p800-or-pa302"
  override def surveyReturnHref: String        = "/personal-account"
  override def surveyReturnMessageKey: String  = "payments-survey.pta.return-message"
  override def surveyIsWelshSupported: Boolean = true
  override def surveyBannerTitle: String       = serviceNameMessageKey

  override def emailTaxTypeMessageKey: String = "email.tax-name.PtaSimpleAssessment"
}
