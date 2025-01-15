package com.tompuri.currencyconverter.http.swop

import com.tompuri.currencyconverter.error.models.internal.InternalApiError.*
import com.tompuri.currencyconverter.http.swop.models.response.RatesResponse
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.client3.testing.*
import sttp.model.{Header, Method, StatusCode}

import scala.concurrent.Future

class SwopHttpClientSpec extends AnyFlatSpec with Matchers with ScalaFutures with EitherValues {

  val defaultApiKey = "test-api-key"
  val defaultHost = "https://swop.cx/rest"

  def defaultClient(backend: SttpBackendStub[Future, Any]) = new SwopHttpClient(defaultApiKey, defaultHost, backend)

  it should "fetch rates successfully" in {
    val expectedResponse = RatesResponse("EUR", "USD", 1.2, "2024-01-01")

    val responseJson =
      s"""
         |{
         |  "base_currency": "${expectedResponse.baseCurrency}",
         |  "quote_currency": "${expectedResponse.quoteCurrency}",
         |  "quote": ${expectedResponse.quote},
         |  "date": "${expectedResponse.date}"
         |}
         |""".stripMargin

    val backend =
      SttpBackendStub.asynchronousFuture
        .whenRequestMatches { req =>
          req.uri.toString.contains(s"$defaultHost/rates/${expectedResponse.baseCurrency}/${expectedResponse.quoteCurrency}") &&
          req.headers.contains(Header("Authorization", s"ApiKey $defaultApiKey")) &&
          req.method == Method.GET
        }
        .thenRespond(responseJson)

    val client = defaultClient(backend)

    val result = client.rates(expectedResponse.baseCurrency, expectedResponse.quoteCurrency).futureValue

    result.value shouldBe RatesResponse("EUR", "USD", 1.2, "2024-01-01")
  }

  it should "handle HTTP error" in {
    val backend = SttpBackendStub.asynchronousFuture.whenAnyRequest
      .thenRespond("Internal Server Error", StatusCode.InternalServerError)

    val client = defaultClient(backend)
    val result = client.rates("EUR", "USD").futureValue

    result.left.value match {
      case HttpError(_, code) => code shouldBe 500
      case _                  => fail("Expected an HttpError")
    }
  }

  it should "handle network error" in {
    val backend = SttpBackendStub.asynchronousFuture.whenAnyRequest
      .thenRespondF(Future.failed(new RuntimeException("Network error")))

    val client = defaultClient(backend)
    val response = client.rates("EUR", "USD").futureValue

    response.left.value shouldBe a[NetworkError]
  }

  it should "handle deserialization error" in {
    val backend = SttpBackendStub.asynchronousFuture.whenAnyRequest
      // not in snake_case
      .thenRespond("""{"baseCurrency": "EUR", "quoteCurrency": "USD", "quote": "1.2", "date": "2024-01-01"}""")

    val client = defaultClient(backend)
    val result = client.rates("EUR", "USD").futureValue

    result.left.value shouldBe a[DeserializationError]
  }
}
