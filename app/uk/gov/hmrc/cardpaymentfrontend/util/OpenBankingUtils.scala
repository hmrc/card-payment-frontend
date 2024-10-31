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

package uk.gov.hmrc.cardpaymentfrontend.util

import payapi.cardpaymentjourney.model.journey._
import payapi.corcommon.model.taxes.cds.CdsRef
import payapi.corcommon.model.taxes.other.XRef
import uk.gov.hmrc.cardpaymentfrontend.config.AppConfig
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking._

import javax.inject.{Inject, Singleton}

@Singleton
class OpenBankingUtils @Inject() (appConfig: AppConfig) {

  def originDataFromJsd(jsd: JourneySpecificData, returnUrl: Option[String]): Option[OriginSpecificSessionData] = {
    val defaultReturnUrl: Option[String] = returnUrl.map(url => if (url.nonEmpty) url else "/business-account")

    jsd match {
      case JsdBtaSa(utr, _, _) => Some(BtaSaSessionData(utr, defaultReturnUrl))
      case JsdPtaSa(utr, _, _) => Some(PtaSaSessionData(utr, returnUrl))
      case JsdPfSa(Some(utr)) => Some(PfSaSessionData(utr))
      case JsdItSa(utr, _) => Some(ItSaSessionData(utr))
      case JsdBtaEpayeBill(accountsOfficeReference, period, _, _) => Some(BtaEpayeBillSessionData(accountsOfficeReference, period, defaultReturnUrl))
      case JsdBtaEpayePenalty(epayePenaltyReference, _, _, _, _) => Some(BtaEpayePenaltySessionData(epayePenaltyReference, defaultReturnUrl))
      case JsdBtaEpayeInterest(xRef, _, _, _, _) => Some(BtaEpayeInterestSessionData(xRef, defaultReturnUrl))
      case JsdBtaEpayeGeneral(accountsOfficeReference, _, Some(period)) => Some(BtaEpayeGeneralSessionData(accountsOfficeReference, period, defaultReturnUrl))
      case JsdBtaClass1aNi(accountsOfficeReference, period, _, _) => Some(BtaClass1aNiSessionData(accountsOfficeReference, period, defaultReturnUrl))
      case JsdBtaCt(utr, Some(period), Some(charge), _, _, _) => Some(BtaCtSessionData(utr, period, charge, defaultReturnUrl))
      case JsdBtaVat(vrn, _, _) => Some(BtaVatSessionData(vrn, defaultReturnUrl))
      case JsdVcVatReturn(vrn, _, _, _) => Some(VcVatReturnSessionData(vrn, returnUrl))
      case JsdVcVatOther(vrn, chargeRef, _, _, _) => Some(VcVatOtherSessionData(vrn, chargeRef, returnUrl))
      case JsdPfVat(maybeVrn, maybeChargeRef) => Some(PfVatSessionData(maybeVrn, maybeChargeRef))
      case JsdPfCt(Some(utr), Some(ctPeriod), Some(ctChargeType)) => Some(PfCtSessionData(utr, ctPeriod, ctChargeType))
      case JsdPfEpayeNi(Some(accountsOfficeReference), Some(period)) => Some(PfEpayeNiSessionData(accountsOfficeReference, period))
      case JsdPfEpayeLpp(Some(prn)) => Some(PfEpayeLppSessionData(prn))
      case JsdPfEpayeSeta(Some(psaNumber)) => Some(PfEpayeSetaSessionData(psaNumber))
      case JsdPfEpayeLateCis(Some(prn)) => Some(PfEpayeLateCisSessionData(prn))
      case JsdPfEpayeP11d(Some(accountsOfficeReference), Some(period)) => Some(PfEpayeP11dSessionData(accountsOfficeReference, period))
      case JsdNiEuVatOss(vrn, period, _, _) => Some(NiEuVatOssSessionData(vrn, period, Some(appConfig.vatOssUrl)))
      case JsdNiEuVatIoss(ioss, period, _, _) => Some(NiEuVatIossSessionData(ioss, period, Some(appConfig.vatIossUrl)))
      case JsdPfNiEuVatOss(Some(vrn), Some(period)) => Some(PfNiEuVatOssSessionData(vrn, period))
      case JsdPfNiEuVatIoss(Some(ioss), Some(period)) => Some(PfNiEuVatIossSessionData(ioss, period))
      case JsdCapitalGainsTax(accountReference, _, _, _) => Some(CapitalGainsTaxSessionData(accountReference, returnUrl))
      case JsdPtaSimpleAssessment(p302Ref, p302ChargeRef, _, _, _) => Some(PtaSimpleAssessmentSessionData(p302Ref, p302ChargeRef, returnUrl))
      case JsdPfSimpleAssessment(Some(xRef)) => Some(PfSimpleAssessmentSessionData(xRef))
      case JsdPfSdlt(Some(utrn)) => Some(PfSdltSessionData(utrn))
      case JsdPfBioFuels(Some(ref)) => Some(PfBioFuelsSessionData(ref))
      case JsdPfGbPbRgDuty(Some(ref)) => Some(PfGbPbRgDutySessionData(ref))
      case JsdPfMgd(Some(ref)) => Some(PfMgdSessionData(ref))
      case JsdPfGamingOrBingoDuty(Some(ref)) => Some(PfGamingOrBingoDutySessionData(XRef(ref.value)))
      case JsdPfAmls(Some(ref)) => Some(PfAmlsSessionData(ref))
      case JsdAmls(ref, _) => Some(AmlsSessionData(ref, returnUrl))
      case JsdPfTpes(Some(ref)) => Some(PfTpesSessionData(ref))
      case JsdPfChildBenefitRepayments(Some(ref)) => Some(PfChildBenefitSessionData(ref))
      case JsdPfAggregatesLevy(Some(ref)) => Some(PfAggregatesLevySessionData(ref))
      case JsdPfLandfillTax(Some(ref)) => Some(PfLandfillTaxSessionData(XRef(ref.value)))
      case JsdPfClimateChangeLevy(Some(ref)) => Some(PfClimateChangeLevySessionData(ref))
      case JsdPfCds(Some(ref), _) => Some(PfCdsSessionData(CdsRef(ref.value)))
      case JsdPfInsurancePremium(Some(ref)) => Some(PfInsurancePremiumSessionData(ref))
      case JsdPfAirPass(Some(ref)) => Some(PfAirPassSessionData(ref))
      case JsdPfClass2Ni(Some(ref)) => Some(PfClass2NiSessionData(ref))
      case JsdPfBeerDuty(Some(ref)) => Some(PfBeerDutySessionData(ref))
      case JsdPfPsAdmin(Some(ref)) => Some(PfPsAdminTaxSessionData(ref))
      case JsdPfClass3Ni(Some(ref)) => Some(PfClass3NiSessionData(ref))
      case JsdPpt(ref, _, _) => Some(PptSessionData(ref, returnUrl))
      case JsdPfPpt(Some(ref)) => Some(PfPptSessionData(ref))
      case JsdPfSdil(Some(ref)) => Some(PfSdilSessionData(ref))
      case JsdBtaSdil(ref, _, _) => Some(BtaSdilSessionData(ref, returnUrl))
      case JsdPfInheritanceTax(Some(ref)) => Some(PfInheritanceTaxSessionData(ref))
      case JsdPfWineAndCider(Some(ref)) => Some(PfWineAndCiderTaxSessionData(ref))
      case JsdPfSpiritsDrinks(Some(ref)) => Some(PfSpiritDrinksSessionData(ref))
      case JsdPfImportedVehicles(Some(ref)) => Some(PfImportedVehiclesSessionData(ref))
      case JsdAppSa(ref, _, _) => Some(AppSaSessionData(ref, defaultReturnUrl))
      case JsdPfAted(Some(ref)) => Some(PfAtedSessionData(ref, defaultReturnUrl))
      case JsdPfCdsDeferment(Some(ref)) => Some(PfCdsDefermentSessionData(ref, defaultReturnUrl))
      case JsdPfCdsCash(Some(ref)) => Some(PfCdsCashSessionData(ref, defaultReturnUrl))
      case JsdPfTrust(Some(ref)) => Some(PfTrustSessionData(ref, defaultReturnUrl))
      case JsdPfEconomicCrimeLevy(Some(ref)) => Some(PfEconomicCrimeLevySessionData(ref, defaultReturnUrl))
      case JsdEconomicCrimeLevy(ref, _, _) => Some(EconomicCrimeLevySessionData(ref, defaultReturnUrl))
      case JsdPtaClass3Ni(ref, _) => Some(PtaClass3NiSessionData(ref, defaultReturnUrl))
      case JsdPfAlcoholDuty(Some(alcoholDutyReference)) => Some(PfAlcoholDutySessionData(alcoholDutyReference, defaultReturnUrl))
      case JsdAlcoholDuty(alcoholDutyReference, alcoholDutyChargeReference, _) => Some(AlcoholDutySessionData(alcoholDutyReference, alcoholDutyChargeReference, defaultReturnUrl))
      case JsdVatC2c(ref, _) => Some(VatC2cSessionData(ref))
      case _ => None
    }
  }

}
