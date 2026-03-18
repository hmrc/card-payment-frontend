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

package uk.gov.hmrc.cardpaymentfrontend.services

import com.google.inject.{Inject, Singleton}
import uk.gov.hmrc.cardpaymentfrontend.crypto.Crypto
import uk.gov.hmrc.cardpaymentfrontend.models.{Address, EmailAddress}

@Singleton
class CryptoService @Inject() (crypto: Crypto) {

  def encryptString(s: String): String = crypto.encrypt(s)

  def decryptString(s: String): String = crypto.decrypt(s)

  def encryptEmail(email: EmailAddress): EmailAddress = {
    EmailAddress(encryptString(email.value))
  }

  def decryptEmail(email: EmailAddress): EmailAddress = {
    EmailAddress(decryptString(email.value))
  }

  def encryptAddress(address: Address): Address = {
    address.copy(
      line1 = encryptString(address.line1),
      line2 = address.line2.map(encryptString),
      city = address.city.map(encryptString),
      county = address.county.map(encryptString),
      postcode = address.postcode.map(encryptString),
      country = encryptString(address.country)
    )
  }

  def decryptAddress(address: Address): Address = {
    address.copy(
      line1 = decryptString(address.line1),
      line2 = address.line2.map(decryptString),
      city = address.city.map(decryptString),
      county = address.county.map(decryptString),
      postcode = address.postcode.map(decryptString),
      country = decryptString(address.country)
    )
  }
}
