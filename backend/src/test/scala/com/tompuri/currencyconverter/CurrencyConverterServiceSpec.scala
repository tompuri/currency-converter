package com.tompuri.currencyconverter

import com.tompuri.currencyconverter.error.models.internal.InternalApiError
import com.tompuri.currencyconverter.http.swop.models.response.RatesResponse
import com.tompuri.currencyconverter.http.swop.{HttpClientCache, SwopHttpClient}
import com.tompuri.currencyconverter.models.ConversionResult
import io.circe
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.annotation.experimental
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future

@experimental
class CurrencyConverterServiceSpec extends AnyFlatSpec with Matchers with MockFactory with ScalaFutures {

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  // scalamock has hard time mocking method signature this complex so it's easier to use a custom stub
  class StubHttpClientCache extends HttpClientCache {
    val keysUsed: ListBuffer[String] = ListBuffer.empty
    var callCount: Int = 0

    override def executeCached[T: circe.Decoder: circe.Encoder](key: String)(
        f: => Future[Either[InternalApiError, T]]
    ): Future[Either[InternalApiError, T]] = {
      keysUsed += key
      callCount += 1
      f
    }
  }

  it should "convert currency based on SWOP quota" in {
    val expectedSourceCurrency = "EUR"
    val expectedTargetCurrency = "USD"

    val responseBody = Right(RatesResponse(expectedSourceCurrency, expectedTargetCurrency, 1.2, "2024-01-01"))
    val mockHttpClient = mock[SwopHttpClient]
    mockHttpClient.rates
      .expects(expectedSourceCurrency, expectedTargetCurrency)
      .returning(Future.successful(responseBody))

    val currencyConverter = new CurrencyConverterService(mockHttpClient, new StubHttpClientCache)
    currencyConverter.convert("EUR", "USD", 1).futureValue shouldBe Right(ConversionResult(1.2, 1.2, "2024-01-01"))
  }

  it should "invoke cache" in {
    val expectedSourceCurrency = "EUR"
    val expectedTargetCurrency = "USD"

    val responseBody = Right(RatesResponse(expectedSourceCurrency, expectedTargetCurrency, 1.2, "2024-01-01"))
    val mockHttpClient = mock[SwopHttpClient]
    val mockHttpClientCache = new StubHttpClientCache
    mockHttpClient.rates
      .expects(expectedSourceCurrency, expectedTargetCurrency)
      .returning(
        Future.successful(responseBody)
      )

    val currencyConverter = new CurrencyConverterService(mockHttpClient, mockHttpClientCache)
    currencyConverter.convert("EUR", "USD", 1).futureValue

    mockHttpClientCache.keysUsed should contain theSameElementsAs List("EUR-USD")
    mockHttpClientCache.callCount shouldBe 1
  }
}
