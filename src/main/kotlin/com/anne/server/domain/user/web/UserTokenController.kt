package com.anne.server.domain.user.web

import com.anne.server.domain.user.dto.response.TokenResponse
import com.anne.server.domain.user.service.UserTokenService
import com.anne.server.global.auth.jwt.AuthenticationService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/user/token")
class UserTokenController (

    private val authenticationService: AuthenticationService,

    private val userTokenService: UserTokenService

) {

    @Operation(summary = "Access Token 재발급")
    @GetMapping("/refresh")
    fun refresh(@RequestHeader("Refresh") refreshToken: String): ResponseEntity<TokenResponse> {
        return ResponseEntity.ok().body(authenticationService.refreshToken(refreshToken))
    }

    @Operation(summary = "Refresh Token 재발급")
    @GetMapping("/refresh/generate")
    fun generateRefreshToken(): ResponseEntity<TokenResponse> {
        return ResponseEntity.ok().body(userTokenService.generateRefreshToken())
    }

}