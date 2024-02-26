package com.fillingsnap.server.global.config.security

import com.fillingsnap.server.domain.user.dto.RefreshTokenDto
import com.fillingsnap.server.domain.user.dto.LoginDto
import com.fillingsnap.server.domain.user.dto.UserDto
import com.fillingsnap.server.domain.user.service.UserService
import com.fillingsnap.server.global.exception.CustomException
import com.fillingsnap.server.global.exception.ErrorCode
import com.fillingsnap.server.infra.redis.RedisDao
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.*

@Service
class JwtProvider (

    @Value("\${spring.jwt.secret}")
    private var secretKey: String,

    @Value("\${spring.jwt.token.access-expiration-time}")
    private val tokenPeriod: Long,

    @Value("\${spring.jwt.token.refresh-expiration-time}")
    private val refreshPeriod: Long,

    private val userService: UserService,

    private val redisDao: RedisDao

) {

    @PostConstruct
    private fun init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.toByteArray())
    }

    fun generateTokenAndRefreshToken(id: String, provider: String, uid: String): LoginDto {
        val user = userService.loadUserById(id.toLong())

        val tokenClaims = Jwts.claims()
            .subject(id)
            .add(mapOf(Pair("provider", provider), Pair("uid", uid)))
            .build()
        val refreshClaims = Jwts.claims()
            .subject(id)
            .build()

        val token = generateToken(tokenPeriod, tokenClaims)
        val refresh = generateToken(refreshPeriod, refreshClaims)
        redisDao.setValues(id, refresh, Duration.ofMillis(refreshPeriod))

        return LoginDto(UserDto(user), token, refresh)
    }

    fun refreshToken(refreshToken: String): RefreshTokenDto {
        val split = refreshToken.split(" ")
        if (split.size != 2 || split[0] != "Bearer") {
            throw CustomException(ErrorCode.INVALID_TOKEN)
        }

        val id = getId(split[1])
        if (split[1] != redisDao.getValues(id)) {
            throw CustomException(ErrorCode.INVALID_TOKEN)
        }

        val user = userService.loadUserById(id.toLong())
        val claims = Jwts.claims()
            .subject(id)
            .add(mapOf(Pair("provider", user.provider), Pair("uid", user.uid)))
            .build()

        return RefreshTokenDto(generateToken(tokenPeriod, claims))
    }

    fun generateToken(period: Long, claims: Claims): String {
        val now = Date()
        return Jwts.builder()
            .claims(claims)
            .issuedAt(now)
            .expiration(Date(now.time + period))
            .signWith(Keys.hmacShaKeyFor(secretKey.toByteArray(Charsets.UTF_8)))
            .compact()
    }

    fun verifyToken(token: String): Boolean {
        return try {
            val user = userService.loadUserById(getId(token).toLong())

            val claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretKey.toByteArray(Charsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
            if (claims.payload["provider"] != user.provider || claims.payload["uid"] != user.uid) {
                return false
            }

            return claims.payload
                .expiration
                .after(Date())
        } catch (e: Exception) {
            false
        }
    }

    fun getId(token: String): String {
        return try {
            Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretKey.toByteArray(Charsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .payload
                .subject
        } catch (e: Exception) {
            throw CustomException(ErrorCode.INVALID_TOKEN)
        }
    }

    fun getAuthentication(token: String): UsernamePasswordAuthenticationToken {
        val id: String = getId(token)
        val user = userService.loadUserById(id.toLong())

        return UsernamePasswordAuthenticationToken(
            user,
            "",
            emptyList()
        )
    }

}