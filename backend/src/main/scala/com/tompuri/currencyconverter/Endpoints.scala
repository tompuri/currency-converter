package com.tompuri.currencyconverter

import com.tompuri.currencyconverter.error.mapper.ErrorMapper
import com.tompuri.currencyconverter.error.models.external.ExternalApiError
import com.tompuri.currencyconverter.error.models.external.ExternalApiError.{BadRequest, Forbidden, InternalServerError}
import com.tompuri.currencyconverter.error.models.internal.InternalApiError
import com.tompuri.currencyconverter.error.models.internal.InternalApiError.NetworkError
import com.tompuri.currencyconverter.models.{ConversionResult, ConvertResponse}
import io.circe.generic.auto.*
import retry.Success
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.swagger.bundle.SwaggerInterpreter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Endpoints(currencyConverterService: CurrencyConverterService) {

  private def currencyPath(name: String) =
    path[String](name)
      .description("ISO 4217 currency code")
      .validate(Validator.pattern("^[A-Z]{3}$"))

  val convertEndpoint = endpoint.get
    .in(
      "convert" /
        currencyPath("source").default("EUR") /
        currencyPath("target").default("USD") /
        path[Double]("value").description("Monetary value to convert").default(1.23)
    )
    .errorOut(
      oneOf[ExternalApiError](
        oneOfVariant(statusCode(StatusCode.BadRequest).and(jsonBody[BadRequest].description("bad request"))),
        oneOfVariant(statusCode(StatusCode.Forbidden).and(jsonBody[Forbidden].description("forbidden"))),
        oneOfVariant(statusCode(StatusCode.InternalServerError).and(jsonBody[InternalServerError].description("internal server error"))),
        oneOfDefaultVariant(jsonBody[InternalServerError].description("internal server error"))
      )
    )
    .out(jsonBody[ConvertResponse])

  val convertEndpointServer: ServerEndpoint[Any, Future] = {
    implicit val success = Success[Either[InternalApiError, ConversionResult]] {
      case Left(NetworkError(_)) => false
      case _                     => true
    }

    convertEndpoint.serverLogic((source, target, value) =>
      retry
        .Directly(1) {
          currencyConverterService.convert(source, target, value)
        }
        .map {
          case Right(result) => Right(result.toResponse)
          case Left(error)   => Left(ErrorMapper.toExternal(error))
        }
    )
  }

  val healthEndpoint = endpoint.get
    .in("health")
    .out(stringBody)
    .description("Health check endpoint")

  val healthEndpointServer: ServerEndpoint[Any, Future] = healthEndpoint.serverLogicSuccess(_ => Future.successful("OK"))

  val apiEndpoints: List[ServerEndpoint[Any, Future]] = List(convertEndpointServer, healthEndpointServer)

  val docEndpoints: List[ServerEndpoint[Any, Future]] = SwaggerInterpreter()
    .fromServerEndpoints[Future](apiEndpoints, "exchange-rates-service", "1.0.0")

  val all: List[ServerEndpoint[Any, Future]] = apiEndpoints ++ docEndpoints
}
