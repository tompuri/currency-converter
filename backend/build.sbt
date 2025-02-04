import com.typesafe.sbt.packager.Keys.dockerExposedPorts
import sbt.Keys.*

val tapirVersion = "1.11.12"
val sttpClientVersion = "3.10.2"

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

lazy val rootProject = (project in file(".")).settings(
  Seq(
    name := "currency-converter-service",
    version := "0.1.0-SNAPSHOT",
    organization := "com.tompuri",
    scalaVersion := "3.3.4",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-armeria-server" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-opentelemetry-metrics" % tapirVersion,
      "com.softwaremill.sttp.client3" %% "core" % sttpClientVersion,
      "com.softwaremill.sttp.client3" %% "circe" % sttpClientVersion,
      "com.softwaremill.retry" %% "retry" % "0.3.6",
      "redis.clients" % "jedis" % "5.2.0",
      "io.opentelemetry" % "opentelemetry-api" % "1.46.0",
      "io.opentelemetry" % "opentelemetry-sdk" % "1.46.0",
      "io.opentelemetry" % "opentelemetry-exporter-logging" % "1.46.0",
      "ch.qos.logback" % "logback-classic" % "1.5.16",
      "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server" % tapirVersion % Test,
      "org.scalatest" %% "scalatest" % "3.2.19" % Test,
      "org.scalamock" %% "scalamock" % "6.1.1" % Test,
      "com.softwaremill.sttp.client3" %% "circe" % sttpClientVersion % Test
    ),
    coverageExcludedFiles := ".*OpenTelemetryConfig.*,.*Main.*",
    Compile / scalacOptions ++= Seq(
      "-Wunused:imports"
    ),
    semanticdbEnabled := true, // for scalafix imports
    dockerBaseImage := "eclipse-temurin:21-jre-ubi9-minimal",
    dockerExposedPorts := Seq(8080)
  )
)
