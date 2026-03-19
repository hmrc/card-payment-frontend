/*
 * Copyright 2026 HM Revenue & Customs
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

import uk.gov.hmrc.cardpaymentfrontend.models.{Address, EmailAddress}
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec

class CryptoServiceSpec extends ItSpec {

  private val service = app.injector.instanceOf[CryptoService]

  "Email Encryption/Decryption" - {

    "encryptEmail returns an encrypted email " in {
      val email          = EmailAddress("test@example.com")
      val encryptedEmail = service.encryptEmail(email)

      encryptedEmail.value should not be email
    }

    "decryptEmail returns the email from the encryptedEmail" in {
      val email          = EmailAddress("some@email.com")
      val encryptedEmail = service.encryptEmail(email)
      val decryptedEmail = service.decryptEmail(encryptedEmail)

      decryptedEmail shouldBe email
    }
  }

  "Address Encryption/Decryption" - {

    "encryptAddress returns an encrypted address " in {
      val address          = Address(
        line1 = "20 Fake Cottage",
        line2 = Some("Fake Street"),
        city = Some("Imaginaryshire"),
        county = Some("East Imaginationland"),
        postcode = Some("IM2 4HJ"),
        country = "GBR"
      )
      val encryptedAddress = service.encryptAddress(address)

      encryptedAddress should not be address
    }

    "decryptAddress returns the address from the encryptedAddress" in {
      val address          = Address(
        line1 = "20 Fake Cottage",
        line2 = Some("Fake Street"),
        city = Some("Imaginaryshire"),
        county = Some("East Imaginationland"),
        postcode = Some("IM2 4HJ"),
        country = "GBR"
      )
      val encryptedAddress = service.encryptAddress(address)
      val decryptedAddress = service.decryptAddress(encryptedAddress)

      decryptedAddress shouldBe address
    }
  }
}
