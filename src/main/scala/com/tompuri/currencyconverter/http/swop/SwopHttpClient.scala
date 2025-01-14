package com.tompuri.currencyconverter.http.swop

import com.tompuri.currencyconverter.http.swop.models.response.RatesResponse
import sttp.client3.SttpBackend
import sttp.client3.UriContext
import sttp.client3.basicRequest
import sttp.client3.circe.*

import scala.concurrent.Future

class SwopHttpClient(apiKey: String, host: String, backend: SttpBackend[Future, Any]) {
  def rates(
      baseCurrency: String,
      quoteCurrency: String
  ): Future[SwopResponse[RatesResponse]] = {
    val request = basicRequest
      .get(uri"$host/rates/$baseCurrency/$quoteCurrency")
      .header("Authorization", s"ApiKey $apiKey")
      .response(asJson[RatesResponse])

    request.send(backend)
  }
}
