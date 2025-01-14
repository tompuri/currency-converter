package com.tompuri.currencyconverter.cache.redis

import scala.concurrent.duration.Duration

class RedisCache(jedis: JedisWrapper) {
  def get(key: String): Option[String] = jedis.get(key)
  def set(key: String, value: String, timeToLive: Duration): Unit = {
    jedis.set(key, value, timeToLive.toSeconds)
  }
}
