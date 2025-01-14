package com.tompuri.currencyconverter

import io.opentelemetry.api.metrics.Meter
import io.opentelemetry.api.metrics.MeterProvider
import org.slf4j.LoggerFactory
import redis.clients.jedis.JedisPooled
import sttp.client3.HttpClientFutureBackend
import sttp.tapir.server.metrics.opentelemetry.OpenTelemetryMetrics
import sttp.tapir.server.netty.NettyFutureServer
import sttp.tapir.server.netty.NettyFutureServerOptions

import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.io.StdIn

import ExecutionContext.Implicits.global
import EnvironmentVariables.*
import com.tompuri.currencyconverter.cache.redis.{JedisWrapper, RedisCache}
import com.tompuri.currencyconverter.http.swop.{SwopCacheExpiryCalculator, SwopHttpClient, SwopHttpClientCache}
import com.tompuri.currencyconverter.observability.opentelemetry.OpenTelemetryConfig
import com.tompuri.currencyconverter.time.DefaultTimeProvider

object Main {
  private val logger = LoggerFactory.getLogger(getClass)

  @main def run(): Unit = {
    val requiredEnvVars = List(
      EnvironmentVariables.SwopApiKey,
      EnvironmentVariables.SwopHost
    )

    val missingEnvVars = requiredEnvVars.filterNot(sys.env.contains)

    if (missingEnvVars.nonEmpty) {
      println(missingEnvVars)
      missingEnvVars.foreach { envVar =>
        logger.error(s"$envVar env not found.")
      }
      logger.info("Shutting down.")
    } else {
      val swopApiKey = sys.env(EnvironmentVariables.SwopApiKey)
      val swopHost = sys.env(EnvironmentVariables.SwopHost).stripSuffix("/")

      val backend = HttpClientFutureBackend()
      val swopHttpClient = new SwopHttpClient(swopApiKey, swopHost, backend)
      val swopCache =
        new SwopHttpClientCache(
          RedisCache(new JedisWrapper(new JedisPooled("localhost", 6379))),
          new SwopCacheExpiryCalculator(new DefaultTimeProvider())
        )
      val currencyConverterService = new CurrencyConverterService(swopHttpClient, swopCache)
      val endpoints = new Endpoints(currencyConverterService)

      val port = sys.env.get(EnvironmentVariables.HttpPort).flatMap(_.toIntOption).getOrElse(8080)

      val meterProvider: MeterProvider = OpenTelemetryConfig.init()
      val meter: Meter = meterProvider.get("instrumentation-name")
      val metrics = OpenTelemetryMetrics.default[Future](meter)
      val metricsInterceptor = metrics.metricsInterceptor()

      val serverOptions = NettyFutureServerOptions.customiseInterceptors
        .metricsInterceptor(metricsInterceptor)
        .options

      val program =
        for
          binding <- NettyFutureServer(serverOptions).port(port).addEndpoints(endpoints.all).start()
          _ <- Future:
            logger.info(s"Go to http://localhost:${binding.port}/docs to open SwaggerUI. Press ENTER key to exit.")
            StdIn.readLine()
          stop <- binding.stop()
        yield stop

      Await.result(program, Duration.Inf)
    }
  }
}
