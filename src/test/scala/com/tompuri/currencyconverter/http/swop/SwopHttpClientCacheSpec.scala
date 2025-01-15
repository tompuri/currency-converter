package com.tompuri.currencyconverter.http.swop

import com.tompuri.currencyconverter.cache.redis.RedisCache
import com.tompuri.currencyconverter.error.models.internal.InternalApiError
import com.tompuri.currencyconverter.error.models.internal.InternalApiError.NetworkError
import io.circe.syntax.*
import org.scalamock.scalatest.MockFactory
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.annotation.experimental
import scala.concurrent.Future

@experimental
class SwopHttpClientCacheSpec extends AnyFlatSpec with Matchers with ScalaFutures with EitherValues with MockFactory {
  it should "cache successful responses" in {
    val cacheKey = "testKey"
    val expectedTtl = 60L

    val redisMock = mock[RedisCache]
    redisMock.get.expects(cacheKey).returning(None).once()
    redisMock.set.expects(cacheKey, "{}".asJson.noSpaces, expectedTtl).once()

    val cache = new SwopHttpClientCache(redisMock, expectedTtl)
    val response: Either[InternalApiError, String] = Right("{}")

    val result = cache.executeCached(cacheKey)(Future.successful(response)).futureValue
    result shouldBe response
  }

  it should "return cached response" in {
    val cacheKey = "testKey"

    val redisMock = mock[RedisCache]
    redisMock.set.expects(*, *, *).never()
    redisMock.get.expects(cacheKey).returning(Some("{}".asJson.noSpaces)).once()

    val cache = new SwopHttpClientCache(redisMock, 60)
    val response: Either[InternalApiError, String] = Right("{}")

    val result = cache.executeCached(cacheKey)(Future.successful(response)).futureValue
    result shouldBe response
  }

  it should "execute request if cache deserialization fails" in {
    val cacheKey = "testKey"

    val redisMock = mock[RedisCache]
    redisMock.get.expects(cacheKey).returning(Some("invalid json")).once()
    redisMock.set.expects(*, *, *).never()

    val cache = new SwopHttpClientCache(redisMock, 60)
    val response: Either[InternalApiError, String] = Right("{}")

    val result = cache.executeCached(cacheKey)(Future.successful(response)).futureValue
    result shouldBe response
  }

  it should "not cache error results" in {
    val cacheKey = "testKey"

    val redisMock = mock[RedisCache]
    redisMock.set.expects(*, *, *).never()
    redisMock.get.expects(cacheKey).returning(None).once()

    val cache = new SwopHttpClientCache(redisMock, 60)
    val response: Either[InternalApiError, String] = Left(NetworkError("test"))

    val result = cache.executeCached(cacheKey)(Future.successful(response)).futureValue
    result shouldBe response
  }
}
