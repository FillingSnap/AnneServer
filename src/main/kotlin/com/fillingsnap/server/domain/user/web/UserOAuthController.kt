package com.fillingsnap.server.domain.user.web

import com.fillingsnap.server.global.auth.oauth.OAuthService
import com.fillingsnap.server.domain.user.dto.LoginDto
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/login/oauth2")
class UserOAuthController (

    private val oAuthService: OAuthService

) {

    @Operation(summary = "OAuth2 코드 발급(리다이렉트)")
    @GetMapping("/code")
    fun getCodeRedirect(
        @RequestParam code: String
    ): ResponseEntity<String> {
        return ResponseEntity.ok(code)
    }

    @Operation(summary = "로그인")
    @GetMapping("/{registrationId}")
    fun getToken(
        @RequestParam code: String,
        @PathVariable("registrationId") registrationId: String
    ): ResponseEntity<LoginDto> {
        return ResponseEntity.ok(oAuthService.login(code, registrationId))
    }

}