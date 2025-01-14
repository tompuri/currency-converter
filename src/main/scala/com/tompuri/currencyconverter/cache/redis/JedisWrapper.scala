package com.tompuri.currencyconverter.cache.redis

import redis.clients.jedis.UnifiedJedis

@SuppressWarnings(Array("scalafix:DisableSyntax.null"))
class JedisWrapper(jedis: UnifiedJedis) {
  def get(key: String): Option[String] = jedis.get(key) match {
    case null  => None
    case value => Some(value)
  }
  def set(key: String, value: String, timeToLive: Long): Unit = {
    jedis.setex(key, timeToLive, value)
  }
}
