package com.tompuri.currencyconverter.time

import java.time.{LocalDateTime, ZoneOffset}

trait TimeProvider {
  def now(): LocalDateTime
}

class DefaultTimeProvider extends TimeProvider {
  override def now(): LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)
}
