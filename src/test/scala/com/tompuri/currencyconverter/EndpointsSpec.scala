package com.tompuri.currencyconverter

import com.tompuri.currencyconverter.error.models.internal.{DeserializationError, NetworkError}
import com.tompuri.currencyconverter.models.ConversionResult
import org.scalamock.scalatest.MockFactory
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.client3.UriContext
import sttp.client3.basicRequest
import sttp.client3.testing.SttpBackendStub
import sttp.model.StatusCode
import sttp.tapir.server.stub.TapirStubInterpreter

import scala.annotation.experimental
import scala.concurrent.Future

@experimental
class EndpointsSpec extends AnyFlatSpec with Matchers with EitherValues with MockFactory with ScalaFutures {
  it should "return conversion result" in {
    val currencyConverterServiceMock = mock[CurrencyConverterService]
    currencyConverterServiceMock.convert
      .expects("EUR", "USD", 1)
      .returning(Future.successful(Right(ConversionResult(1.2, 1.2, "2024-01-01"))))

    val endpoints = new Endpoints(currencyConverterServiceMock)

    val backendStub = TapirStubInterpreter(SttpBackendStub.asynchronousFuture)
      .whenServerEndpointRunLogic(endpoints.convertEndpointServer)
      .backend()

    val response = basicRequest.get(uri"https://test.com/convert/EUR/USD/1").send(backendStub).futureValue

    response.code shouldBe StatusCode(200)
    response.body.value shouldBe """{"value":1.2,"quote":1.2,"quoteDate":"2024-01-01"}"""
  }

  it should "retry once on network error" in {
    val currencyConverterServiceMock = stub[CurrencyConverterService]
    currencyConverterServiceMock.convert
      .when(*, *, *)
      .returning(Future.successful(Left(NetworkError("Network error"))))

    val endpoints = new Endpoints(currencyConverterServiceMock)

    val backendStub = TapirStubInterpreter(SttpBackendStub.asynchronousFuture)
      .whenServerEndpointRunLogic(endpoints.convertEndpointServer)
      .backend()

    basicRequest.get(uri"https://test.com/convert/EUR/USD/1").send(backendStub).futureValue

    currencyConverterServiceMock.convert.verify("EUR", "USD", 1).twice()
  }

  it should "not retry on deserialization error" in {
    val currencyConverterServiceMock = stub[CurrencyConverterService]
    currencyConverterServiceMock.convert
      .when(*, *, *)
      .returning(Future.successful(Left(DeserializationError("Network error"))))

    val endpoints = new Endpoints(currencyConverterServiceMock)

    val backendStub = TapirStubInterpreter(SttpBackendStub.asynchronousFuture)
      .whenServerEndpointRunLogic(endpoints.convertEndpointServer)
      .backend()

    basicRequest.get(uri"https://test.com/convert/EUR/USD/1").send(backendStub).futureValue

    currencyConverterServiceMock.convert.verify("EUR", "USD", 1).once()
  }
}
