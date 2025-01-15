package com.tompuri.currencyconverter.error.models.external

enum ExternalApiError(val message: String) {
  case BadRequest(override val message: String) extends ExternalApiError(message)
  case Forbidden(override val message: String) extends ExternalApiError(message)
  case InternalServerError(override val message: String) extends ExternalApiError(message)
}
