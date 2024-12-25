package com.anne.server.domain.user.service

import com.anne.server.domain.user.domain.User
import com.anne.server.domain.user.dto.UserDto
import com.anne.server.domain.user.dto.response.TokenResponse
import com.anne.server.global.auth.jwt.AuthenticationService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class UserTokenService (

    private val authenticationService: AuthenticationService

) {

    fun generateRefreshToken(): TokenResponse {
        val userDto = SecurityContextHolder.getContext().authentication.principal as UserDto
        return TokenResponse(authenticationService.generateRefreshToken(userDto.id.toString()))
    }

}