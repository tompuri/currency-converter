package com.tompuri.currencyconverter.http.swop

import com.tompuri.currencyconverter.time.TimeProvider

import java.time.temporal.ChronoUnit
import scala.concurrent.duration.Duration

class SwopCacheExpiryCalculator(timeProvider: TimeProvider) {
  def timeToLive(): Duration = {
    val now = timeProvider.now()
    val nextHour = now.plusHours(1).truncatedTo(ChronoUnit.HOURS)
    // SWOP API returns exchange rates for the current day, but we don't know the timezone of the server
    // as we don't want to assume anything it's safer to use ttl until next hour
    // TODO: Optimization opportunity: we could cache the exchange rates for the whole day
    Duration(ChronoUnit.SECONDS.between(now, nextHour), scala.concurrent.duration.SECONDS)
  }
}
