package com.fillingsnap.server.infra.redis

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration


@Component
class RedisDao(private val redisTemplate: RedisTemplate<String, String>) {
    fun setValues(key: String, data: String) {
        val values = redisTemplate.opsForValue()
        values[key] = data
    }

    fun setValues(key: String, data: String, duration: Duration) {
        val values = redisTemplate.opsForValue()
        values.set(key, data, duration)
    }

    fun getValues(key: String?): String? {
        val values = redisTemplate.opsForValue()
        return values[key!!]
    }

    fun deleteValues(key: String) {
        redisTemplate.delete(key)
    }
}