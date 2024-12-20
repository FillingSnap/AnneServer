package com.anne.server.domain.user.service

import com.anne.server.domain.user.domain.User
import com.anne.server.domain.user.dto.response.TokenResponse
import com.anne.server.global.auth.jwt.JwtAuthenticationService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class UserTokenService (

    private val jwtAuthenticationService: JwtAuthenticationService

) {

    fun generateRefreshToken(): TokenResponse {
        val user = SecurityContextHolder.getContext().authentication.principal as User
        return TokenResponse(jwtAuthenticationService.generateRefreshToken(user.id!!.toString()))
    }

}