package com.tompuri.currencyconverter

import com.tompuri.currencyconverter.error.models.internal.{DeserializationError, HttpError, InternalApiError, NetworkError}
import com.tompuri.currencyconverter.http.swop.{HttpClientCache, SwopHttpClient}
import com.tompuri.currencyconverter.models.ConversionResult
import org.slf4j.LoggerFactory
import sttp.client3.Response

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CurrencyConverterService(httpClient: SwopHttpClient, cache: HttpClientCache) {

  private val logger = LoggerFactory.getLogger(getClass)

  def convert(source: String, target: String, value: Double): Future[Either[InternalApiError, ConversionResult]] = {
    cache
      .executeCached(s"$source-$target") {
        httpClient.rates(source, target)
      }
      .map {
        case Response(Right(rateResponse), _, _, _, _, _) => {
          val quote = BigDecimal(rateResponse.quote)
          val newValue = CurrencyConverter.convert(BigDecimal(value), quote)
          Right(ConversionResult(newValue, quote, rateResponse.date))
        }
        case Response(Left(error), statusCode, _, _, _, request) =>
          statusCode.code match {
            case code if code >= 400 && code <= 599 =>
              logger.error(s"HTTP error: $error, Request: $request")
              Left(HttpError(error.toString, code))
            case _ =>
              logger.error(s"Deserialization error: $error, Request: $request")
              Left(DeserializationError(error.toString))
          }
      }
      .recover { case e: Exception =>
        logger.error("Failed to convert currency.", e)
        Left(NetworkError(e.getMessage))
      }
  }
}
