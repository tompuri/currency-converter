package com.tompuri.currencyconverter.error.models.external

sealed trait ExternalApiError {
  def message: String
}

case class BadRequest(message: String) extends ExternalApiError
case class Forbidden(message: String) extends ExternalApiError
case class InternalServerError(message: String) extends ExternalApiError
