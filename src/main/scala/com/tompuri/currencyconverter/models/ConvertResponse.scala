package com.tompuri.currencyconverter.models

import sttp.tapir.Schema.annotations.description
import sttp.tapir.Schema.annotations.encodedName

@encodedName("Ok")
case class ConvertResponse(
    @description("The converted monetary value") value: Double,
    @description("The exchange rate used for the conversion") quote: Double,
    @description("The date of the exchange rate") quoteDate: String
)
