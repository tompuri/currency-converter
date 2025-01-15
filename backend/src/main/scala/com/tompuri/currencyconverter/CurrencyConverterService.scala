package com.tompuri.currencyconverter

import com.tompuri.currencyconverter.error.models.internal.InternalApiError
import com.tompuri.currencyconverter.http.swop.{HttpClientCache, SwopHttpClient}
import com.tompuri.currencyconverter.models.ConversionResult

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CurrencyConverterService(httpClient: SwopHttpClient, cache: HttpClientCache) {

  def convert(source: String, target: String, value: Double): Future[Either[InternalApiError, ConversionResult]] = {
    cache
      .executeCached(s"$source-$target") {
        httpClient.rates(source, target)
      }
      .map(_.map { rateResponse =>
        val quote = BigDecimal(rateResponse.quote)
        val newValue = CurrencyConverter.convert(BigDecimal(value), quote)
        ConversionResult(newValue, quote, rateResponse.date)
      })
  }
}
