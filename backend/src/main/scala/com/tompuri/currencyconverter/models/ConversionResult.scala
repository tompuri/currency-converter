package com.tompuri.currencyconverter.models

case class ConversionResult(value: BigDecimal, quote: BigDecimal, quoteDate: String) {
  def toResponse: ConvertResponse = ConvertResponse(value.toDouble, quote.toDouble, quoteDate)
}
