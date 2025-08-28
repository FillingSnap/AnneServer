package com.anne.server.domain.user.web

import com.anne.server.domain.user.dto.response.LoginResponse
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
@RequestMapping("/api/login")
class UserLoginController (

    private val userLoginService: UserLoginService

) {

    @Operation(summary = "FedCM 로그인")
    @GetMapping("/fedCM/{registrationId}")
    fun login(
        @RequestParam idToken: String,
        @PathVariable("registrationId") registrationId: String
    ): ResponseEntity<LoginResponse> {
        return ResponseEntity.ok(userLoginService.login(idToken, registrationId, LoginType.FEDCM))
    }

}