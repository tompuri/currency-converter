package com.tompuri.currencyconverter.models

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ConversionResultSpec extends AnyFlatSpec with Matchers {
  it should "convert result to response model" in {
    val result = ConversionResult(1, 2, "2024-01-01")
    val response = result.toResponse
    response shouldBe ConvertResponse(1, 2, "2024-01-01")
  }
}
