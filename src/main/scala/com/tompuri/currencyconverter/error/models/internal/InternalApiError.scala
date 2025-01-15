package com.tompuri.currencyconverter.error.models.internal

enum InternalApiError(val message: String) {
  case HttpError(override val message: String, httpCode: Int) extends InternalApiError(message)
  case DeserializationError(override val message: String) extends InternalApiError(message)
  case NetworkError(override val message: String) extends InternalApiError(message)
}
