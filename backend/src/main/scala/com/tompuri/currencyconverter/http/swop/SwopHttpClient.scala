package com.tompuri.currencyconverter.http.swop

import com.tompuri.currencyconverter.error.models.internal.InternalApiError
import com.tompuri.currencyconverter.error.models.internal.InternalApiError.{DeserializationError, HttpError, NetworkError}
import com.tompuri.currencyconverter.http.swop.models.response.RatesResponse
import org.slf4j.LoggerFactory
import sttp.client3.circe.*
import sttp.client3.{Response, SttpBackend, UriContext, basicRequest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SwopHttpClient(apiKey: String, host: String, backend: SttpBackend[Future, Any]) {

  val logger = LoggerFactory.getLogger(getClass)

  def rates(
      baseCurrency: String,
      quoteCurrency: String
  ): Future[Either[InternalApiError, RatesResponse]] = {
    val request = basicRequest
      .get(uri"$host/rates/$baseCurrency/$quoteCurrency")
      .header("Authorization", s"ApiKey $apiKey")
      .response(asJson[RatesResponse])

    request
      .send(backend)
      .map {
        case Response(Right(rateResponse), _, _, _, _, _) =>
          Right(rateResponse)
        case Response(Left(error), statusCode, _, _, _, _) =>
          statusCode.code match {
            case code if code >= 400 && code <= 599 =>
              logger.error(s"Failed to fetch rates for $baseCurrency/$quoteCurrency", error)
              Left(HttpError(error.toString, code))
            case _ =>
              logger.error(s"Failed to fetch rates for $baseCurrency/$quoteCurrency", error)
              Left(DeserializationError(error.toString))
          }
      }
      .recover { case e: Exception =>
        logger.error(s"Failed to fetch rates for $baseCurrency/$quoteCurrency", e)
        Left(NetworkError(e.getMessage))
      }
  }
}
