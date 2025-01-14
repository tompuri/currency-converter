package com.tompuri.currencyconverter.http.swop.models.response

import io.circe.Decoder
import io.circe.Encoder

case class RatesResponse(baseCurrency: String, quoteCurrency: String, quote: Double, date: String)

object RatesResponse {
  implicit val decodeRate: Decoder[RatesResponse] =
    Decoder.forProduct4("base_currency", "quote_currency", "quote", "date")(RatesResponse.apply)

  implicit val encodeRate: Encoder[RatesResponse] =
    Encoder.forProduct4("base_currency", "quote_currency", "quote", "date")(r => (r.baseCurrency, r.quoteCurrency, r.quote, r.date))
}
