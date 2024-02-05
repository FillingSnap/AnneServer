package com.fillingsnap.server.domain.user.service

import com.fillingsnap.server.domain.user.dto.TokenDto
import com.fillingsnap.server.global.exception.CustomException
import com.fillingsnap.server.global.exception.ErrorCode
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service
import java.util.*

@Service
class TokenService (

    private val userService: UserService

) {

    private var secretKey = "9a4f2c8d3b7a1e6f45c8a0b3f267d8b1d4e6f3c8a9d2b5f8e3a9c8b5f6v8a3d9"

    @PostConstruct
    private fun init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.toByteArray())
    }

    fun generateToken(id: String): TokenDto {
        val tokenPeriod = 1000L * 60L * 10L
        val refreshPeriod = 1000L * 60L * 60L * 24L * 30L * 3L

        val claims = Jwts.claims().setSubject(id)

        val now = Date()
        return TokenDto(
            Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(Date(now.time + tokenPeriod))
                .signWith(Keys.hmacShaKeyFor(secretKey.toByteArray(Charsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact(),
            Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(Date(now.time + refreshPeriod))
                .signWith(Keys.hmacShaKeyFor(secretKey.toByteArray(Charsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact()
        )
    }

    fun verifyToken(token: String): Boolean {
        return try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(secretKey.toByteArray(Charsets.UTF_8))
                .build()
                .parseClaimsJws(token)
            claims.body
                .expiration
                .after(Date())
        } catch (e: Exception) {
            false
        }
    }

    fun getId(token: String): String {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey.toByteArray(Charsets.UTF_8))
            .build()
            .parseClaimsJws(token)
            .body
            .subject
    }

    fun getAuthentication(token: String): UsernamePasswordAuthenticationToken {
        val id: String = getId(token)
        val user = userService.loadUserById(id.toLong()) ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        return UsernamePasswordAuthenticationToken(
            user,
            "",
            emptyList()
        )
    }

}