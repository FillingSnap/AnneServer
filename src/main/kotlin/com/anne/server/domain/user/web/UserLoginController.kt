package com.anne.server.domain.user.web

import com.anne.server.domain.user.dto.response.UserLoginResponseDto
import com.anne.server.domain.user.enums.LoginType
import com.anne.server.domain.user.service.UserLoginService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/login")
class UserLoginController (

    private val userLoginService: UserLoginService

) {

    @Operation(summary = "OAuth2 코드 발급(리다이렉트)")
    @GetMapping("/oauth2/code")
    fun getCodeRedirect(
        @RequestParam code: String
    ): ResponseEntity<String> {
        return ResponseEntity.ok(code)
    }

    @Operation(summary = "OAuth2 로그인")
    @GetMapping("/oauth2/{registrationId}")
    fun getToken(
        @RequestParam code: String,
        @PathVariable("registrationId") registrationId: String
    ): ResponseEntity<UserLoginResponseDto> {
        return ResponseEntity.ok(userLoginService.login(code, registrationId, LoginType.OAUTH))
    }

    @Operation(summary = "FedCM 로그인")
    @GetMapping("/fedCM/{registrationId}")
    fun login(
        @RequestParam idToken: String,
        @PathVariable("registrationId") registrationId: String
    ): ResponseEntity<UserLoginResponseDto> {
        return ResponseEntity.ok(userLoginService.login(idToken, registrationId, LoginType.FEDCM))
    }

}