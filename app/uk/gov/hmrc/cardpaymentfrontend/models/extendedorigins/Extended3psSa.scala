package uk.gov.hmrc.cardpaymentfrontend.models.extendedorigins

import payapi.cardpaymentjourney.model.journey.{JourneySpecificData, Jsd3psSa}
import play.api.mvc.AnyContent
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.PaymentMethod.OpenBanking
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking.{OriginSpecificSessionData, `3psSaSessionData`}
import uk.gov.hmrc.cardpaymentfrontend.models.{CheckYourAnswersRow, PaymentMethod}

object Extended3psSa extends ExtendedOrigin {
  override val serviceNameMessageKey: String = "service-name.3psSa"
  override val taxNameMessageKey: String = "payment-complete.tax-name.3psSa"

  override def cardFeesPagePaymentMethods: Set[PaymentMethod] = Set()

  override def paymentMethods(): Set[PaymentMethod] = Set(OpenBanking)

  override def checkYourAnswersReferenceRow(journeyRequest: JourneyRequest[AnyContent])
                                           (payFrontendBaseUrl: String): Option[CheckYourAnswersRow] = {
    Some(CheckYourAnswersRow(
      titleMessageKey = "check-your-details.3psSa.reference",
      value           = Seq(journeyRequest.journey.referenceValue),
      changeLink      = None
    ))
  }

  override def openBankingOriginSpecificSessionData: JourneySpecificData => Option[OriginSpecificSessionData] = {
    case j: Jsd3psSa => Some(`3psSaSessionData`(j.utr))
    case _           => throw new RuntimeException("Incorrect origin found")
  }

  override def emailTaxTypeMessageKey: String = "email.tax-name.3psSa"
  override def surveyAuditName: String = "self-assessment"
  override def surveyReturnHref: String = "https://www.gov.uk/government/organisations/hm-revenue-customs"
  override def surveyReturnMessageKey: String = "payments-survey.other.return-message"
  override def surveyIsWelshSupported: Boolean = true
  override def surveyBannerTitle: String = serviceNameMessageKey

}
