package com.tompuri.currencyconverter

import com.linecorp.armeria.server.Server
import com.tompuri.currencyconverter.cache.redis.{JedisWrapper, RedisCache}
import com.tompuri.currencyconverter.http.swop.{SwopHttpClient, SwopHttpClientCache}
import com.tompuri.currencyconverter.observability.opentelemetry.OpenTelemetryConfig
import io.opentelemetry.api.metrics.{Meter, MeterProvider}
import org.slf4j.LoggerFactory
import redis.clients.jedis.JedisPooled
import sttp.client3.HttpClientFutureBackend
import sttp.tapir.server.armeria.{ArmeriaFutureServerInterpreter, ArmeriaFutureServerOptions}
import sttp.tapir.server.metrics.opentelemetry.OpenTelemetryMetrics

import scala.concurrent.duration.{Duration, MINUTES}
import scala.concurrent.{ExecutionContext, Future}

import ExecutionContext.Implicits.global
import EnvironmentVariables.*

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

      val redisHost = sys.env.getOrElse(EnvironmentVariables.RedisHost, "localhost")
      val redisPort = sys.env.get(EnvironmentVariables.RedisPort).flatMap(_.toIntOption).getOrElse(6379)
      val redis = RedisCache(new JedisWrapper(new JedisPooled(redisHost, redisPort)))

      val redisTimeToLive =
        sys.env
          .get(EnvironmentVariables.RedisTimeToLiveInSeconds)
          .flatMap(_.toLongOption)
          .getOrElse(Duration(5, MINUTES).toSeconds)
      val swopCache = new SwopHttpClientCache(redis, redisTimeToLive)

      val currencyConverterService = new CurrencyConverterService(swopHttpClient, swopCache)

      val endpoints = new Endpoints(currencyConverterService)

      val port = sys.env.get(EnvironmentVariables.HttpPort).flatMap(_.toIntOption).getOrElse(8080)

      val meterProvider: MeterProvider = OpenTelemetryConfig.init()
      val meter: Meter = meterProvider.get("instrumentation-name")
      val metrics = OpenTelemetryMetrics.default[Future](meter)
      val metricsInterceptor = metrics.metricsInterceptor()

      val serverOptions = ArmeriaFutureServerOptions.customiseInterceptors
        .metricsInterceptor(metricsInterceptor)
        .options

      val tapirService = ArmeriaFutureServerInterpreter(serverOptions).toService(endpoints.all)
      val server = Server
        .builder()
        .service(tapirService)
        .http(port)
        .build()

      Future {
        Thread.sleep(1000)
        logger.info(s"Go to http://localhost:${port}/docs to open SwaggerUI.")
      }

      server.start().join()
    }
  }
}
