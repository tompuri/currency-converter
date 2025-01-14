package com.tompuri.currencyconverter.http.swop.models.response

import io.circe.syntax.*
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RatesResponseSpec extends AnyFlatSpec with Matchers with EitherValues {
  val response = RatesResponse("EUR", "USD", 1.2, "2024-01-01")
  val json = """{"base_currency":"EUR","quote_currency":"USD","quote":1.2,"date":"2024-01-01"}"""

  it should "deserialize json to RatesResponse" in {
    val ratesResponse = io.circe.parser.decode[RatesResponse](json)
    ratesResponse.value shouldBe response
  }

  it should "serialize RatesResponse to json" in {
    response.asJson.noSpaces shouldBe json
  }
}
