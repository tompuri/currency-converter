package com.tompuri.currencyconverter

import com.tompuri.currencyconverter.error.models.internal.{DeserializationError, HttpError, NetworkError}
import com.tompuri.currencyconverter.http.swop.models.response.RatesResponse
import com.tompuri.currencyconverter.http.swop.{HttpClientCache, SwopHttpClient, SwopResponse}
import com.tompuri.currencyconverter.models.ConversionResult
import io.circe
import io.circe.DecodingFailure
import io.circe.DecodingFailure.Reason.CustomReason
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.client3.{Response, ResponseException, ShowError}
import sttp.model.StatusCode

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
        f: => Future[SwopResponse[T]]
    ): Future[SwopResponse[T]] = {
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
      .returning(
        Future.successful(Response(responseBody, StatusCode(200)))
      )

    val currencyConverter = new CurrencyConverterService(mockHttpClient, new StubHttpClientCache)
    currencyConverter.convert("EUR", "USD", 1).futureValue shouldBe Right(ConversionResult(1.2, 1.2, "2024-01-01"))
  }

  it should "return HttpError if client returns HttpError" in {
    val expectedSourceCurrency = "EUR"
    val expectedTargetCurrency = "USD"

    val responseBody = Left(sttp.client3.HttpError("Internal Server Error", StatusCode(500)))
    val mockHttpClient = mock[SwopHttpClient]
    mockHttpClient.rates
      .expects(expectedSourceCurrency, expectedTargetCurrency)
      .returning(
        Future.successful(Response(responseBody, StatusCode(500)))
      )

    val currencyConverter = new CurrencyConverterService(mockHttpClient, new StubHttpClientCache)

    currencyConverter.convert("EUR", "USD", 1).futureValue match {
      case Left(HttpError(_, code)) => code shouldBe 500
      case _                        => fail("Expected an HttpError")
    }
  }

  it should "return DeserializationError if client returns DeserializationException" in {
    val expectedSourceCurrency = "EUR"
    val expectedTargetCurrency = "USD"

    val responseBody: Either[ResponseException[String, io.circe.Error], RatesResponse] =
      Left(sttp.client3.DeserializationException("{}", DecodingFailure(CustomReason("Decoding error message"), List.empty)))
    val mockHttpClient = mock[SwopHttpClient]
    mockHttpClient.rates
      .expects(expectedSourceCurrency, expectedTargetCurrency)
      .returning(
        Future.successful(Response(responseBody, StatusCode(200)))
      )

    val currencyConverter = new CurrencyConverterService(mockHttpClient, new StubHttpClientCache)

    currencyConverter.convert("EUR", "USD", 1).futureValue match {
      case Left(DeserializationError(_)) => succeed
      case _                             => fail("Expected an DeserializationError")
    }
  }

  it should "return NetworkError if client throws an exception" in {
    val expectedSourceCurrency = "EUR"
    val expectedTargetCurrency = "USD"

    val mockHttpClient = mock[SwopHttpClient]
    mockHttpClient.rates
      .expects(expectedSourceCurrency, expectedTargetCurrency)
      .returning(
        Future.failed(new RuntimeException("Network error"))
      )

    val currencyConverter = new CurrencyConverterService(mockHttpClient, new StubHttpClientCache)

    currencyConverter.convert("EUR", "USD", 1).futureValue match {
      case Left(NetworkError(_)) => succeed
      case _                     => fail("Expected an NetworkError")
    }
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
        Future.successful(Response(responseBody, StatusCode(200)))
      )

    val currencyConverter = new CurrencyConverterService(mockHttpClient, mockHttpClientCache)
    currencyConverter.convert("EUR", "USD", 1).futureValue

    mockHttpClientCache.keysUsed should contain theSameElementsAs List("EUR-USD")
    mockHttpClientCache.callCount shouldBe 1
  }
}
