package com.tompuri.currencyconverter

object CurrencyConverter {
  def convert(value: BigDecimal, quote: BigDecimal): BigDecimal = value * quote
}
