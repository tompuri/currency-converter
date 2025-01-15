package com.tompuri.currencyconverter.time

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.{LocalDateTime, ZoneOffset}

class DefaultTimeProviderSpec extends AnyFlatSpec with Matchers {

  "DefaultTimeProvider" should "return the current time in UTC" in {
    val timeProvider = new DefaultTimeProvider
    val now = LocalDateTime.now(ZoneOffset.UTC)
    val providedTime = timeProvider.now()

    // Allow a small margin of error for the time difference
    providedTime should be >= now.minusSeconds(1)
    providedTime should be <= now.plusSeconds(1)
  }
}
