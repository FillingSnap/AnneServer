package com.anne.server.domain.user.web

import com.anne.server.domain.user.dto.response.TokenResponseDto
import com.anne.server.domain.user.service.UserService
import com.anne.server.domain.user.service.UserTokenService
import com.anne.server.global.auth.jwt.JwtAuthenticationService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/user/token")
class UserTokenController (

    private val jwtAuthenticationService: JwtAuthenticationService,

    private val userTokenService: UserTokenService

) {

    @Operation(summary = "Access Token 재발급")
    @GetMapping("/refresh")
    fun refresh(@RequestHeader("Refresh") refreshToken: String): ResponseEntity<TokenResponseDto> {
        return ResponseEntity.ok().body(jwtAuthenticationService.refreshToken(refreshToken))
    }

    @Operation(summary = "Refresh Token 재발급")
    @GetMapping("/refresh/generate")
    fun generateRefreshToken(): ResponseEntity<TokenResponseDto> {
        return ResponseEntity.ok().body(userTokenService.generateRefreshToken())
    }

}