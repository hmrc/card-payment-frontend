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

import payapi.cardpaymentjourney.model.journey.{JourneySpecificData, JsdAppSimpleAssessment}
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.PaymentMethod.{Bacs, Card}
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking.{AppSimpleAssessmentSessionData, OriginSpecificSessionData}
import uk.gov.hmrc.cardpaymentfrontend.models.{CheckYourAnswersRow, PaymentMethod}

object ExtendedAppSimpleAssessment extends ExtendedOrigin {

  override def serviceNameMessageKey: String = "service-name.AppSimpleAssessment"
  override def taxNameMessageKey: String     = "payment-complete.tax-name.AppSimpleAssessment"

  override def cardFeesPagePaymentMethods: Set[PaymentMethod] = Set(Bacs)

  override def paymentMethods(): Set[PaymentMethod] = Set(Card, Bacs)

  override def checkYourAnswersReferenceRow(journeyRequest: JourneyRequest[AnyContent])(payFrontendBaseUrl: String): Option[CheckYourAnswersRow] = {
    Some(
      CheckYourAnswersRow(
        titleMessageKey = "check-your-details.AppSimpleAssessment.reference",
        value = Seq(journeyRequest.journey.referenceValue),
        changeLink = None
      )
    )
  }

  override def checkYourAnswersAdditionalReferenceRow(
    journeyRequest: JourneyRequest[AnyContent]
  )(payFrontendBaseUrl: String)(implicit messages: Messages): Option[Seq[CheckYourAnswersRow]] = {
    journeyRequest.journey.journeySpecificData match {
      case j: JsdAppSimpleAssessment =>
        val adjustedTaxYear = j.taxYear.nextTaxYear
        Some(
          Seq(
            CheckYourAnswersRow(
              titleMessageKey = "check-your-details.AppSimpleAssessment.tax-year",
              value = Seq(
                messages(
                  "check-your-details.AppSimpleAssessment.tax-year.value",
                  adjustedTaxYear.startYear.toString,
                  adjustedTaxYear.endYear.toString
                )
              ),
              changeLink = None
            )
          )
        )

      case _ => throw new RuntimeException("Incorrect origin found")
    }
  }

  override def openBankingOriginSpecificSessionData: JourneySpecificData => Option[OriginSpecificSessionData] = {
    case j: JsdAppSimpleAssessment => Some(AppSimpleAssessmentSessionData(j.p302Ref))
    case _                         => throw new RuntimeException("Incorrect origin found")
  }

  override def emailTaxTypeMessageKey: String  = "email.tax-name.AppSimpleAssessment"
  override def surveyAuditName: String         = "simple-assessment"
  override def surveyReturnHref: String        = "https://www.gov.uk/government/organisations/hm-revenue-customs"
  override def surveyReturnMessageKey: String  = "payments-survey.other.return-message"
  override def surveyIsWelshSupported: Boolean = true
  override def surveyBannerTitle: String       = serviceNameMessageKey
}
