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

import payapi.cardpaymentjourney.model.journey.{JourneySpecificData, JsdPfCt}
import payapi.corcommon.model.Reference
import payapi.corcommon.model.taxes.ReferenceMaker.makeCtReference
import play.api.i18n.Messages
import play.api.mvc.{AnyContent, Call}
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.PaymentMethod.{Bacs, Card, OneOffDirectDebit, OpenBanking}
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking.{OriginSpecificSessionData, PfCtSessionData}
import uk.gov.hmrc.cardpaymentfrontend.models.{CheckYourAnswersRow, Link, PaymentMethod}

object ExtendedPfCt extends ExtendedOrigin {
  override val serviceNameMessageKey: String = "service-name.PfCt"
  override val taxNameMessageKey: String = "payment-complete.tax-name.PfCt"

  def cardFeesPagePaymentMethods: Set[PaymentMethod] = Set(OpenBanking, OneOffDirectDebit)

  def paymentMethods(): Set[PaymentMethod] = Set(Card, OpenBanking, OneOffDirectDebit, Bacs)

  override def reference(request: JourneyRequest[AnyContent]): String = {
    request.journey.journeySpecificData match {
      case j: JsdPfCt => j.utr.map(_.canonicalizedValue).getOrElse(throw new RuntimeException("Missing ctUtr"))
      case _          => throw new RuntimeException("Incorrect origin found")
    }
  }

  private def payslipReference: JourneySpecificData => Option[Reference] = {
    case j: JsdPfCt => for {
      ctUtr <- j.utr
      ctPeriod <- j.ctPeriod
      ctChargeType <- j.ctChargeType
    } yield makeCtReference(ctUtr, ctPeriod, ctChargeType)
    case _ => throw new RuntimeException("Incorrect origin found")
  }

  override def checkYourAnswersReferenceRow(journeyRequest: JourneyRequest[AnyContent])(payFrontendBaseUrl: String): Option[CheckYourAnswersRow] = {
    Some(CheckYourAnswersRow(
      titleMessageKey = "check-your-details.PfCt.reference",
      value           = Seq(reference(journeyRequest)),
      changeLink      = None
    ))
  }

  override def checkYourAnswersAdditionalReferenceRow(journeyRequest: JourneyRequest[AnyContent])
    (payFrontendBaseUrl: String)
    (implicit messages: Messages): Option[Seq[CheckYourAnswersRow]] = {
    payslipReference(journeyRequest.journey.journeySpecificData).map { payslipReference =>
      Seq(CheckYourAnswersRow(
        titleMessageKey = "check-your-details.PfCt.payslip-reference",
        value           = Seq(payslipReference.value),
        changeLink      = Some(Link(
          href       = Call("GET", changeReferenceUrl(payFrontendBaseUrl)),
          linkId     = "check-your-details-reference-change-link",
          messageKey = "check-your-details.change"
        ))
      ))
    }
  }

  override def openBankingOriginSpecificSessionData: JourneySpecificData => Option[OriginSpecificSessionData] = {
    case j: JsdPfCt => for {
      ctUtr <- j.utr
      ctPeriod <- j.ctPeriod
      ctChargeType <- j.ctChargeType
    } yield PfCtSessionData(ctUtr, ctPeriod, ctChargeType)
    case _ => throw new RuntimeException("Incorrect origin found")

  }

  override def emailTaxTypeMessageKey: String = "email.tax-name.PfCt"

  override def surveyAuditName: String = "corporation-tax"
  override def surveyReturnHref: String = "https://www.gov.uk/government/organisations/hm-revenue-customs"
  override def surveyReturnMessageKey: String = "payments-survey.other.return-message"
  override def surveyIsWelshSupported: Boolean = true
  override def surveyBannerTitle: String = serviceNameMessageKey
}
