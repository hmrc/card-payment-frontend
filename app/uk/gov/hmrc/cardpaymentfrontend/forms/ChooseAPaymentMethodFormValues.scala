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

package uk.gov.hmrc.cardpaymentfrontend.forms

import enumeratum.{Enum, EnumEntry}

import scala.collection.immutable

sealed trait ChooseAPaymentMethodFormValues extends EnumEntry with Product with Serializable

object ChooseAPaymentMethodFormValues extends Enum[ChooseAPaymentMethodFormValues] {

  case object OpenBanking extends ChooseAPaymentMethodFormValues
  case object TryAgain    extends ChooseAPaymentMethodFormValues

  override val values: immutable.IndexedSeq[ChooseAPaymentMethodFormValues] = findValues
}
