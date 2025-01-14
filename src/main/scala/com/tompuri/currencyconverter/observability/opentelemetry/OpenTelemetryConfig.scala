package com.tompuri.currencyconverter.observability.opentelemetry

import io.opentelemetry.api.metrics.MeterProvider
import io.opentelemetry.exporter.logging.LoggingMetricExporter
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import io.opentelemetry.sdk.metrics.`export`.PeriodicMetricReader

object OpenTelemetryConfig {
  def init(): MeterProvider = {
    val metricExporter = LoggingMetricExporter.create()
    val metricReader = PeriodicMetricReader.builder(metricExporter).build()
    SdkMeterProvider.builder().registerMetricReader(metricReader).build()
  }
}
