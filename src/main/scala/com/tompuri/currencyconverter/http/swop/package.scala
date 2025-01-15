package com.tompuri.currencyconverter.http

import io.circe
import sttp.client3.{Response, ResponseException}

package object swop {
  type SwopResponse[T] = Response[Either[ResponseException[String, circe.Error], T]]
}
