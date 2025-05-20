package uk.gov.hmrc.cardpaymentfrontend.utils

import java.time.LocalDateTime

trait Clock {
  def now: LocalDateTime
}

case class FrozenClock(fixedTime: LocalDateTime) extends Clock {
  override def now: LocalDateTime = fixedTime
}

