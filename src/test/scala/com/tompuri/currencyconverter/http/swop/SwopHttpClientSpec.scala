package com.tompuri.currencyconverter.http.swop

import com.tompuri.currencyconverter.http.swop.models.response.RatesResponse
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.client3.*
import sttp.client3.testing.*
import sttp.model.{Header, Method, StatusCode}

class SwopHttpClientSpec extends AnyFlatSpec with Matchers with ScalaFutures with EitherValues {

  it should "fetch rates successfully" in {
    val apiKey = "test-api-key"
    val host = "https://swop.cx/rest"

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
          req.uri.toString.contains(s"$host/rates/${expectedResponse.baseCurrency}/${expectedResponse.quoteCurrency}") &&
          req.headers.contains(Header("Authorization", s"ApiKey $apiKey")) &&
          req.method == Method.GET
        }
        .thenRespond(responseJson)

    val client = new SwopHttpClient(apiKey, host, backend)

    val result = client.rates(expectedResponse.baseCurrency, expectedResponse.quoteCurrency).futureValue

    result.code shouldBe StatusCode.Ok
    result.body.value shouldBe RatesResponse("EUR", "USD", 1.2, "2024-01-01")
  }

  it should "handle deserialization error" in {
    val backend = SttpBackendStub.asynchronousFuture.whenAnyRequest
      // not in snake_case
      .thenRespond("""{"baseCurrency": "EUR", "quoteCurrency": "USD", "quote": "1.2", "date": "2024-01-01"}""")

    val client = new SwopHttpClient("test-api-key", "https://swop.cx/rest", backend)

    val result = client.rates("EUR", "USD").futureValue

    result.code shouldBe StatusCode.Ok
    result.body.left.value shouldBe a[ResponseException[String, io.circe.Error]]
  }
}
