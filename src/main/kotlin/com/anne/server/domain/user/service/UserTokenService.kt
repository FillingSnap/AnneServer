package com.anne.server.domain.user.service

import com.anne.server.domain.user.domain.User
import com.anne.server.domain.user.dto.response.TokenResponseDto
import com.anne.server.global.auth.jwt.JwtAuthenticationService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class UserTokenService (

    private val jwtAuthenticationService: JwtAuthenticationService

) {

    fun generateRefreshToken(): TokenResponseDto {
        val user = SecurityContextHolder.getContext().authentication.principal as User
        return TokenResponseDto(jwtAuthenticationService.generateRefreshToken(user.id!!.toString()))
    }

}