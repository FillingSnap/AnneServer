package com.fillingsnap.server.domain.user.web

import com.fillingsnap.server.domain.user.service.LoginService
import com.fillingsnap.server.domain.user.dto.TokenDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/login/oauth2")
class LoginController (

    private val loginService: LoginService

) {

    @GetMapping("/code")
    fun getCodeRedirect(
        @RequestParam code: String
    ): ResponseEntity<String> {
        return ResponseEntity.ok(code)
    }

    @GetMapping("/{registrationId}")
    fun getToken(
        @RequestParam code: String,
        @PathVariable("registrationId") registrationId: String
    ): ResponseEntity<TokenDto> {
        return ResponseEntity.ok(loginService.login(code, registrationId))
    }

}