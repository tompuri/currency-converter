package com.tompuri.currencyconverter.http.swop

import com.tompuri.currencyconverter.cache.redis.RedisCache
import io.circe.syntax.*
import org.scalamock.scalatest.MockFactory
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.client3.Response
import sttp.model.StatusCode

import scala.annotation.experimental
import scala.concurrent.Future
import scala.concurrent.duration.*

@experimental
class SwopHttpClientCacheSpec extends AnyFlatSpec with Matchers with ScalaFutures with EitherValues with MockFactory {
  it should "cache successful responses" in {
    val cacheKey = "testKey"
    val expectedTtl = 1.seconds

    val redisMock = mock[RedisCache]
    redisMock.get.expects(cacheKey).returning(None).once()
    redisMock.set.expects(cacheKey, "{}".asJson.noSpaces, expectedTtl).once()

    val cacheExpiryCalculatorMock = mock[SwopCacheExpiryCalculator]
    (() => cacheExpiryCalculatorMock.timeToLive()).expects().returns(expectedTtl).once()

    val cache = new SwopHttpClientCache(redisMock, cacheExpiryCalculatorMock)
    val response: SwopResponse[String] = Response(Right("{}"), StatusCode(200), "")

    val result = cache.executeCached(cacheKey)(Future.successful(response)).futureValue
    result shouldBe response
  }

  it should "return cached response" in {
    val cacheKey = "testKey"

    val redisMock = mock[RedisCache]
    redisMock.set.expects(*, *, *).never()
    redisMock.get.expects(cacheKey).returning(Some("{}".asJson.noSpaces)).once()

    val cacheExpiryCalculatorMock = mock[SwopCacheExpiryCalculator]
    (() => cacheExpiryCalculatorMock.timeToLive()).expects().never()

    val cache = new SwopHttpClientCache(redisMock, cacheExpiryCalculatorMock)
    val response: SwopResponse[String] = Response(Right("{}"), StatusCode(200), "")

    val result = cache.executeCached(cacheKey)(Future.successful(response)).futureValue
    result shouldBe response
  }

  it should "execute request if cache deserialization fails" in {
    val cacheKey = "testKey"
    val expectedTtl = 1.seconds

    val redisMock = mock[RedisCache]
    redisMock.get.expects(cacheKey).returning(Some("invalid json")).once()
    redisMock.set.expects(*, *, *).never()

    val cacheExpiryCalculatorMock = mock[SwopCacheExpiryCalculator]
    (() => cacheExpiryCalculatorMock.timeToLive()).expects().returns(expectedTtl).never()

    val cache = new SwopHttpClientCache(redisMock, cacheExpiryCalculatorMock)
    val response: SwopResponse[String] = Response(Right("{}"), StatusCode(200), "")

    val result = cache.executeCached(cacheKey)(Future.successful(response)).futureValue
    result shouldBe response
  }

  it should "not cache error results" in {
    val cacheKey = "testKey"

    val redisMock = mock[RedisCache]
    redisMock.set.expects(*, *, *).never()
    redisMock.get.expects(cacheKey).returning(None).once()

    val cacheExpiryCalculatorMock = mock[SwopCacheExpiryCalculator]
    (() => cacheExpiryCalculatorMock.timeToLive()).expects().never()

    val cache = new SwopHttpClientCache(redisMock, cacheExpiryCalculatorMock)
    val response: SwopResponse[String] = Response(Right("{}"), StatusCode(400), "")

    val result = cache.executeCached(cacheKey)(Future.successful(response)).futureValue
    result shouldBe response
  }
}
