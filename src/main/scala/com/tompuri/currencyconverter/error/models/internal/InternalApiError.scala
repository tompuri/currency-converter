package com.tompuri.currencyconverter.error.models.internal

sealed trait InternalApiError {
  def message: String
}

case class HttpError(message: String, httpCode: Int) extends InternalApiError
case class DeserializationError(message: String) extends InternalApiError
case class NetworkError(message: String) extends InternalApiError
