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
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.core.env.Environment
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.*

@Service
class JwtProvider (

    private val userService: UserService,

    private val redisDao: RedisDao,

    private val env: Environment

) {

    private val secretKey = env.getProperty("spring.jwt.secret")!!

    private val tokenPeriod = env.getProperty("spring.jwt.token.access-expiration-time")!!.toLong()

    private val refreshPeriod = env.getProperty("spring.jwt.token.refresh-expiration-time")!!.toLong()

    fun generateTokenAndRefreshToken(id: String, provider: String, uid: String): LoginDto {
        val user = userService.loadUserById(id.toLong())

        val tokenClaims = Jwts.claims().setSubject(id)
        tokenClaims.putAll(mapOf(Pair("provider", provider), Pair("uid", uid)))
        val refreshClaims = Jwts.claims().setSubject(id)

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
        val claims = Jwts.claims().setSubject(id)
        claims.putAll(mapOf(Pair("provider", user.provider), Pair("uid", user.uid)))

        return RefreshTokenDto(generateToken(tokenPeriod, claims))
    }

    fun generateToken(period: Long, claims: Claims): String {
        val now = Date()
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(Date(now.time + period))
            .signWith(Keys.hmacShaKeyFor(secretKey.toByteArray(Charsets.UTF_8)), SignatureAlgorithm.HS256)
            .compact()
    }

    fun verifyToken(token: String): Boolean {
        return try {
            val user = userService.loadUserById(getId(token).toLong())

            val claims = Jwts.parserBuilder()
                .setSigningKey(secretKey.toByteArray(Charsets.UTF_8))
                .build()
                .parseClaimsJws(token)
            if (claims.body["provider"] != user.provider || claims.body["uid"] != user.uid) {
                return false
            }

            return claims.body
                .expiration
                .after(Date())
        } catch (e: Exception) {
            false
        }
    }

    fun getId(token: String): String {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey.toByteArray(Charsets.UTF_8))
                .build()
                .parseClaimsJws(token)
                .body
                .subject
        } catch (e: Exception) {
            println(e)
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