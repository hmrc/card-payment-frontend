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

package uk.gov.hmrc.cardpaymentfrontend.models

import uk.gov.hmrc.cardpaymentfrontend.testsupport.UnitSpec

class AddressSpec extends UnitSpec {
  val address: Address                = Address(line1 = "line1", postcode = Some("AA0AA0"), country = "GBR")
  val badStrings: Seq[Option[String]] = Seq(
    Some("(Select entry)"),
    Some("Select"),
    Some("Select..."),
    Some("Select state"),
    Some("Select a region.."),
    Some("– Select -"),
    Some("Select..."),
    Some("–Please Select -"),
    Some("-- Select a State -"),
    Some("Select a State"),
    Some("Please Select"),
    Some("select County"),
    Some("(select entry)"),
    Some("select"),
    Some("select..."),
    Some("select state"),
    Some("select a region.."),
    Some("– select -"),
    Some("select..."),
    Some("–Please select -"),
    Some("-- select a State -"),
    Some("select a State"),
    Some("Please select"),
    Some("select County"),
    Some("SELECT * FROM address")
  )

  // Far from a complete list and takes no account of variations
  val administrativeCounties: Seq[Option[String]] = Seq(
    Some("Bedfordshire"),
    Some("Berkshire"),
    Some("Buckinghamshire"),
    Some("Cambridgeshire"),
    Some("Cheshire"),
    Some("Cornwall"),
    Some("County Durham"),
    Some("Cumberland"),
    Some("Derbyshire"),
    Some("Devon"),
    Some("Dorset"),
    Some("East Riding of Yorkshire"),
    Some("Essex"),
    Some("Gloucestershire"),
    Some("Hampshire"),
    Some("Herefordshire"),
    Some("Herefordshire"),
    Some("Huntingdonshire"),
    Some("Kent"),
    Some("Lancashire"),
    Some("Lincolnshire"),
    Some("London"),
    Some("Middlesex"),
    Some("Monmouthshire"),
    Some("Norfolk"),
    Some("North Riding of Yorkshire"),
    Some("Northamptonshire"),
    Some("Peterborough"),
    Some("Northumberland"),
    Some("Oxfordshire"),
    Some("Rutland"),
    Some("Shropshire"),
    Some("Somerset"),
    Some("Staffordshire"),
    Some("Staffordshire"),
    Some("East Suffolk"),
    Some("West Suffolk"),
    Some("Surrey"),
    Some("East Sussex"),
    Some("West Sussex"),
    Some("Warwickshire"),
    Some("Westmorland"),
    Some("West Riding of Yorkshire"),
    Some("Wiltshire"),
    Some("Worcestershire")
  )

  "Address hasSelect function should recognise variations in Select and select" in {
    badStrings map (s => address.hasSelect(s)) should contain only true
  }

  "Address hasSelect function should not recognise variations in counties" in {
    administrativeCounties map (s => address.hasSelect(s)) should contain only false
  }

  "an Address with a uk country and a bad county name should have the countyName replaced with an None value" in {
    val addressWithBadCounty = Address(line1 = "line1", postcode = Some("AA0AA0"), county = Some("Select ..."), country = "GBR")
    addressWithBadCounty.sanitiseCounty().county shouldBe None
  }

  "an Address with a uk country and a good county name should not have the countyName replaced with an None value" in {
    val addressWithBadCounty = Address(line1 = "line1", postcode = Some("AA0AA0"), county = Some("West Sussex"), country = "GBR")
    addressWithBadCounty.sanitiseCounty().county shouldBe Some("West Sussex")
  }

  "an Address with non uk country and a bad county name should not have the countyName replaced with an None value" in {
    val addressWithBadCounty = Address(line1 = "line1", postcode = Some("AA0AA0"), county = Some("west selectashire"), country = "US")
    addressWithBadCounty.sanitiseCounty().county shouldBe Some("west selectashire")
  }

}
