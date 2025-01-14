package com.tompuri.currencyconverter.http.swop

import scala.concurrent.Future

trait HttpClientCache {
  def executeCached[T: io.circe.Decoder: io.circe.Encoder](key: String)(
      f: => Future[SwopResponse[T]]
  ): Future[SwopResponse[T]]
}
