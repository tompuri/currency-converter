package com.tompuri.currencyconverter.cache.redis

class RedisCache(jedis: JedisWrapper) {
  def get(key: String): Option[String] = jedis.get(key)
  def set(key: String, value: String, timeToLiveInSeconds: Long): Unit = {
    jedis.set(key, value, timeToLiveInSeconds)
  }
}
