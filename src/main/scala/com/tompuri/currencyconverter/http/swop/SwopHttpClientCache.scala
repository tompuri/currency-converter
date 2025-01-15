package com.tompuri.currencyconverter.http.swop

import com.tompuri.currencyconverter.cache.redis.RedisCache
import io.circe
import io.circe.parser.*
import io.circe.syntax.*
import org.slf4j.LoggerFactory
import sttp.client3.{Response, ResponseException}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class SwopHttpClientCache(cache: RedisCache, cacheExpiryCalculator: SwopCacheExpiryCalculator) extends HttpClientCache {
  val logger = LoggerFactory.getLogger(getClass)

  def executeCached[T: io.circe.Decoder: io.circe.Encoder](key: String)(
      f: => Future[SwopResponse[T]]
  ): Future[SwopResponse[T]] = {
    cache.get(key) match {
      case Some(value) =>
        decode[T](value) match {
          case Right(decodedValue) =>
            Future.successful(Response.ok(Right(decodedValue)))
          case Left(error) =>
            logger.error(s"Failed to decode cached value for key: $key", error)
            f
        }
      case None =>
        f.map { response =>
          response match {
            case Response(Right(response), status, _, _, _, _) if status.code == 200 =>
              cache.set(key, response.asJson.noSpaces, cacheExpiryCalculator.timeToLive())
            case _ =>
          }
          response
        }
    }
  }
}
