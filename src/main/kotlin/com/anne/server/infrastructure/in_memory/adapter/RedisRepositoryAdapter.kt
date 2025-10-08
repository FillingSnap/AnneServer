package com.anne.server.infrastructure.in_memory.adapter

import com.anne.server.application.user.port.out.InMemoryDbRepository
import com.anne.server.common.exception.CustomException
import com.anne.server.common.exception.ErrorCode
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisRepositoryAdapter(

    private val redisTemplate: RedisTemplate<String, String>

): InMemoryDbRepository {

    override fun setValue(key: String, data: String, duration: Duration): String {
        try {
            redisTemplate.opsForValue().set(key, data, duration)
        } catch (_: Exception) {
            throw CustomException(ErrorCode.IN_MEMORY_DB_ERROR)
        }

        return data
    }

    override fun getValue(key: String): String? = try {
        redisTemplate.opsForValue()[key]
    } catch (_: Exception) {
        throw CustomException(ErrorCode.IN_MEMORY_DB_ERROR)
    }


}