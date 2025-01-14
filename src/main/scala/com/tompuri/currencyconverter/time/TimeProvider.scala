package com.tompuri.currencyconverter.time

import java.time.LocalDateTime
import java.time.ZoneOffset

trait TimeProvider {
  def now(): LocalDateTime
}

class DefaultTimeProvider extends TimeProvider {
  override def now(): LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)
}
