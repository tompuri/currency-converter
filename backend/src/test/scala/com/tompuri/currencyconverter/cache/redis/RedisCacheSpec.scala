package com.tompuri.currencyconverter.cache.redis

import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.annotation.experimental

@experimental
class RedisCacheSpec extends AnyFlatSpec with Matchers with MockFactory {

  it should "get a value from the cache" in {
    val expectedKey = "testKey"
    val expectedValue = Some("testValue")

    val jedisMock = mock[JedisWrapper]
    jedisMock.get.expects(expectedKey).returning(expectedValue).once()

    val cache = new RedisCache(jedisMock)
    cache.get(expectedKey) should be(expectedValue)
  }

  it should "set a value in the cache" in {
    val expectedKey = "testKey"
    val expectedValue = "testValue"
    val expectedTtl = 60L

    val jedisMock = mock[JedisWrapper]
    jedisMock.set.expects(expectedKey, expectedValue, expectedTtl).once()

    val cache = new RedisCache(jedisMock)
    cache.set(expectedKey, expectedValue, expectedTtl)
  }
}
