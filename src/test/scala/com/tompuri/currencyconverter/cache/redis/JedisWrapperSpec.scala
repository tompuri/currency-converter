package com.tompuri.currencyconverter.cache.redis

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import redis.clients.jedis.UnifiedJedis

import scala.collection.mutable

class JedisWrapperSpec extends AnyFlatSpec with Matchers with EitherValues {

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  class JedisStub extends UnifiedJedis {
    private val cache: mutable.Map[String, String] = mutable.Map.empty

    override def get(key: String): String = {
      cache.getOrElse(key, null)
    }

    override def setex(key: String, seconds: Long, value: String): String = {
      cache.put(key, value)
      value
    }
  }

  it should "return value from the cache" in {
    val expectedKey = "testKey"
    val expectedValue = "testValue"

    val cache = new JedisWrapper(new JedisStub)
    cache.set(expectedKey, expectedValue, 60)
    cache.get(expectedKey) shouldBe Some(expectedValue)
  }

  it should "return None if key is not found in the cache" in {
    val cache = new JedisWrapper(new JedisStub)
    cache.get("testKey") shouldBe None
  }
}
