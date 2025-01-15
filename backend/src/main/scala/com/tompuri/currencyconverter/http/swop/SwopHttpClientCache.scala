package com.tompuri.currencyconverter.http.swop

import com.tompuri.currencyconverter.cache.redis.RedisCache
import com.tompuri.currencyconverter.error.models.internal.InternalApiError
import io.circe
import io.circe.parser.*
import io.circe.syntax.*
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class SwopHttpClientCache(cache: RedisCache, timeToLiveInSeconds: Long) extends HttpClientCache {
  val logger = LoggerFactory.getLogger(getClass)

  def executeCached[T: io.circe.Decoder: io.circe.Encoder](key: String)(
      f: => Future[Either[InternalApiError, T]]
  ): Future[Either[InternalApiError, T]] = {
    cache.get(key) match {
      case Some(value) =>
        decode[T](value) match {
          case Right(decodedValue) =>
            Future.successful(Right(decodedValue))
          case Left(error) =>
            logger.error(s"Failed to decode cached value for key: $key", error)
            f
        }
      case None =>
        f.map { response =>
          response match {
            case Right(response) =>
              cache.set(key, response.asJson.noSpaces, timeToLiveInSeconds)
            case _ =>
          }
          response
        }
    }
  }
}
