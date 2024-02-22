package com.fillingsnap.server.infra.redis

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.core.env.Environment
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories
import org.springframework.data.redis.serializer.StringRedisSerializer


@Configuration
@EnableRedisRepositories
@PropertySource("classpath:application.yml")
class RedisConfig(

    private val env: Environment

) {

    private val host = env.getProperty("spring.data.redis.host")!!

    private val port = env.getProperty("spring.data.redis.port")!!.toInt()

    @Bean
    fun redisConnectionFactory() : LettuceConnectionFactory {
        return LettuceConnectionFactory(host, port)
    }

    @Bean
    fun redisTemplate(): RedisTemplate<String, String> {
        // redisTemplate를 받아와서 set, get, delete를 사용
        val redisTemplate = RedisTemplate<String, String>()
        // setKeySerializer, setValueSerializer 설정
        // redis-cli을 통해 직접 데이터를 조회 시 알아볼 수 없는 형태로 출력되는 것을 방지
        redisTemplate.keySerializer = StringRedisSerializer()
        redisTemplate.valueSerializer = StringRedisSerializer()
        redisTemplate.connectionFactory = redisConnectionFactory()

        return redisTemplate
    }

}