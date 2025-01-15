package com.tompuri.currencyconverter.error.mapper

import com.tompuri.currencyconverter.error.models.external.{BadRequest, ExternalApiError, Forbidden, InternalServerError}
import com.tompuri.currencyconverter.error.models.internal.{HttpError, InternalApiError}
import com.tompuri.currencyconverter.error.models.{external, internal}

object ErrorMapper {
  def toExternal(error: InternalApiError): ExternalApiError = {
    error match {
      case HttpError(_, 400) =>
        BadRequest("Invalid input: Please ensure that both currency codes are in the correct ISO 4217 format (e.g., USD, EUR).")
      case HttpError(_, 403) =>
        Forbidden("Invalid target currency: Ensure the currency is supported and uses the ISO 4217 format (e.g., USD).")
      case _ =>
        InternalServerError("We encountered an unexpected error. Please try again later. If the issue persists, contact support.")
    }
  }
}
