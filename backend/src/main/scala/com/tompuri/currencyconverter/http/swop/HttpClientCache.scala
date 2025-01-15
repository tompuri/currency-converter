package com.tompuri.currencyconverter.http.swop

import com.tompuri.currencyconverter.error.models.internal.InternalApiError

import scala.concurrent.Future

trait HttpClientCache {
  def executeCached[T: io.circe.Decoder: io.circe.Encoder](key: String)(
      f: => Future[Either[InternalApiError, T]]
  ): Future[Either[InternalApiError, T]]
}
