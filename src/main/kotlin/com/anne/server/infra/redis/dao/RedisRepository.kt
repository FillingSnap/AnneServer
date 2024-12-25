package com.anne.server.infra.redis.dao

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration


@Component
class RedisRepository (

    private val redisTemplate: RedisTemplate<String, String>

) {

    fun setValues(key: String, data: String, duration: Duration) {
        val values = redisTemplate.opsForValue()
        values.set(key, data, duration)
    }

    fun getValues(key: String?): String? {
        val values = redisTemplate.opsForValue()
        return values[key!!]
    }

}