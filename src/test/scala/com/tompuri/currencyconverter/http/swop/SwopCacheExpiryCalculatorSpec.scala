package com.tompuri.currencyconverter.http.swop

import com.tompuri.currencyconverter.time.TimeProvider
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks

import java.time.LocalDateTime
import scala.annotation.experimental

@experimental
class SwopCacheExpiryCalculatorSpec extends AnyFlatSpec with Matchers with MockFactory with TableDrivenPropertyChecks {
  it should "return seconds to next hour" in {

    val testCases = Table(
      ("currentTime", "expectedSeconds"),
      (LocalDateTime.of(2024, 1, 1, 0, 0, 0), 3600L),
      (LocalDateTime.of(2024, 1, 1, 0, 30, 0), 1800L),
      (LocalDateTime.of(2024, 1, 1, 0, 59, 59), 1L)
    )

    forAll(testCases) { (currentTime: LocalDateTime, expectedSeconds: Long) =>
      val timeProviderMock = mock[TimeProvider]
      (() => timeProviderMock.now()).expects().returns(currentTime)
      val calculator = new SwopCacheExpiryCalculator(timeProviderMock)
      calculator.timeToLive().toSeconds shouldBe expectedSeconds
    }
  }
}
