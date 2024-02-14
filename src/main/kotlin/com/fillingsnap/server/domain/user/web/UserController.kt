package com.fillingsnap.server.domain.user.web

import com.fillingsnap.server.domain.user.dto.RefreshTokenDto
import com.fillingsnap.server.domain.user.service.TokenService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/user")
class UserController (

    private val tokenService: TokenService

) {

    @Operation(summary = "토큰 재발급")
    @GetMapping("/refresh")
    fun refresh(@RequestHeader("Refresh") refreshToken: String): ResponseEntity<RefreshTokenDto> {
        return ResponseEntity.ok().body(tokenService.refreshToken(refreshToken))
    }

}