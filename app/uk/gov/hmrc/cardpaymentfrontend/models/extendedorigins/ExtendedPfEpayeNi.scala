package uk.gov.hmrc.cardpaymentfrontend.models.extendedorigins

import payapi.cardpaymentjourney.model.journey.{JourneySpecificData, JsdPfEpayeNi}
import play.api.mvc.{AnyContent, Call}
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.PaymentMethod.{Card, OneOffDirectDebit, OpenBanking, VariableDirectDebit}
import uk.gov.hmrc.cardpaymentfrontend.models.{CheckYourAnswersRow, Link, PaymentMethod}
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking.{OriginSpecificSessionData, PfEpayeNiSessionData}


object ExtendedPfEpayeNi extends ExtendedOrigin {

  override def serviceNameMessageKey: String = "service-name.PfEpayeNi"

  override def taxNameMessageKey: String = "payment-complete.tax-name.PfEpayeNi"

  override def cardFeesPagePaymentMethods: Set[PaymentMethod] = Set(OpenBanking, OneOffDirectDebit, VariableDirectDebit)

  override def paymentMethods(): Set[PaymentMethod] = Set(OneOffDirectDebit, VariableDirectDebit, Card, OpenBanking)

  override def checkYourAnswersReferenceRow(journeyRequest: JourneyRequest[AnyContent])
                                           (payFrontendBaseUrl: String): Option[CheckYourAnswersRow] =
    Some(CheckYourAnswersRow(
      titleMessageKey = "check-your-details.PfEpayeNi.reference",
      value           = Seq(journeyRequest.journey.referenceValue),
      changeLink      = Some(Link(
        href       = Call("GET", changeReferenceUrl(payFrontendBaseUrl)),//TODO: may neeed chaging to this //pay/accounts-office-reference/change-reference-number
        linkId     = "check-your-details-reference-change-link",
        messageKey = "check-your-details.change"
      ))
    ))

  override def openBankingOriginSpecificSessionData: JourneySpecificData => Option[OriginSpecificSessionData] = {
    case j: JsdPfEpayeNi => j.accountsOfficeReference.flatMap { accountsOfficeReference =>
      j.period.map(PfEpayeNiSessionData(accountsOfficeReference, _))
    }
    case _ => throw new RuntimeException("Incorrect origin found")
  }

  override def emailTaxTypeMessageKey: String = "email.tax-name.PfEpayeNi"

  override def surveyAuditName: String = "paye-ni"

  override def surveyReturnHref: String = "https://www.gov.uk/government/organisations/hm-revenue-customs"

  override def surveyReturnMessageKey: String = "payments-survey.other.return-message"

  override def surveyIsWelshSupported: Boolean = true

  override def surveyBannerTitle: String = serviceNameMessageKey
}
