package com.anne.server.presentation.api.user.controller

import com.anne.server.application.user.port.`in`.TokenRefreshUseCase
import com.anne.server.application.user.dto.AuthPrincipal
import com.anne.server.common.exception.ErrorCode
import com.anne.server.common.exception.CustomException
import com.anne.server.presentation.api.user.dto.TokenResponse
import com.anne.server.presentation.security.auth.AuthHeaderUtil
import io.swagger.v3.oas.annotations.Operation
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user/token")
class TokenController(

    private val tokenRefreshUseCase: TokenRefreshUseCase

) {

    @Operation(summary = "Access Token 재발급")
    @GetMapping("/refresh")
    fun refresh(
        @RequestHeader("Refresh") refreshToken: String
    ): TokenResponse =
        TokenResponse(
            tokenRefreshUseCase.refreshAccessToken(
                AuthHeaderUtil.extractBearer(refreshToken)
                    ?: throw CustomException(ErrorCode.INVALID_TOKEN)
            )
        )

    @Operation(summary = "Refresh Token 재발급")
    @GetMapping("/refresh/generate")
    fun generateRefreshToken(
        @AuthenticationPrincipal principal: AuthPrincipal
    ): TokenResponse =
        TokenResponse(tokenRefreshUseCase.generateRefreshToken(principal.id))

}