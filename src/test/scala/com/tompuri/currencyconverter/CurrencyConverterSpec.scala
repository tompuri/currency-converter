package com.tompuri.currencyconverter

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks

class CurrencyConverterSpec extends AnyFlatSpec with Matchers with TableDrivenPropertyChecks {

  val testCases = Table(
    ("value", "quote", "expected"),
    ("1", "2", "2"), // Integer values
    ("1.5", "2", "3"), // Decimal value
    ("-1.5", "2", "-3"), // Negative value
    ("1.5", "1.5", "2.25"), // Both value and quote are decimals
    ("0.1", "0.2", "0.02"), // Edge case for floating point arithmetic
    ("0", "2", "0"), // Zero value
    ("1", "0", "0"), // Zero quote
    ("1.123456789123456789", "2", "2.246913578246913578") // High precision
  )

  forAll(testCases) { (value, quote, expected) =>
    it should s"convert $value with quote $quote to $expected" in {
      CurrencyConverter.convert(BigDecimal(value), BigDecimal(quote)) shouldBe BigDecimal(expected)
    }
  }
}
