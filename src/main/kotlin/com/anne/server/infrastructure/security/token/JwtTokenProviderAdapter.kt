package com.anne.server.infrastructure.security.token

import com.anne.server.application.user.dto.AuthPrincipal
import com.anne.server.application.user.port.out.TokenProvider
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date

@Component
class JwtTokenProviderAdapter(

    @Value("\${jwt.secret}")
    private var secret: String,

    @Value("\${jwt.token.access-expiration-time}")
    private val tokenPeriod: Long,

    @Value("\${jwt.token.refresh-expiration-time}")
    private val refreshPeriod: Long

): TokenProvider {

    private val key = Keys.hmacShaKeyFor(secret.toByteArray(Charsets.UTF_8))

    private val parser = Jwts.parser().verifyWith(key).build()

    override fun generateAccessToken(id: String, provider: String, uid: String): String {
        val tokenClaims = Jwts.claims()
            .subject(id)
            .add(mapOf(Pair("provider", provider), Pair("uid", uid)))
            .build()

        return generateToken(tokenPeriod, tokenClaims)
    }

        override fun validateAccessToken(token: String): AuthPrincipal? = try {
            val claims = parser.parseSignedClaims(token).payload

            AuthPrincipal(
                id = claims.subject.toLong(),
                provider = claims["provider"] as String,
                uid = claims["uid"] as String,
            )
        } catch (_: Exception) {
            null
        }

    override fun generateRefreshToken(id: String): String {
        val refreshClaims = Jwts.claims()
            .subject(id)
            .build()

        return generateToken(refreshPeriod, refreshClaims)
    }

    override fun validateRefreshToken(token: String): String? = try {
        parser.parseSignedClaims(token).payload.subject
    } catch (_: Exception) {
        null
    }

    private fun generateToken(period: Long, claims: Claims): String {
        val now = Date()
        return Jwts.builder()
            .claims(claims)
            .issuedAt(now)
            .expiration(Date(now.time + period))
            .signWith(key)
            .compact()
    }

}