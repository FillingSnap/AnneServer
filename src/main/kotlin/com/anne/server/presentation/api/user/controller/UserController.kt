package com.anne.server.presentation.api.user.controller

import com.anne.server.application.user.port.`in`.LoginUseCase
import com.anne.server.application.user.port.`in`.UserStyleUseCase
import com.anne.server.application.user.dto.AuthPrincipal
import com.anne.server.application.user.dto.LoginInfo
import io.swagger.v3.oas.annotations.Operation
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class UserController(

    private val loginUseCase: LoginUseCase,

    private val userStyleUseCase: UserStyleUseCase

){

    @Operation(summary = "회원가입 & 로그인")
    @GetMapping("/login/fedCM/{registrationId}")
    fun login(
        @RequestParam idToken: String,
        @PathVariable("registrationId") registrationId: String
    ): LoginInfo =
        loginUseCase.login(idToken, registrationId)

    @Operation(summary = "일기 작성 스타일 리스트 조회")
    @GetMapping("/user/style")
    fun getStyleList(
        @AuthenticationPrincipal principal: AuthPrincipal
    ): List<String> =
        userStyleUseCase.getStyleList(principal.id)

    @Operation(summary = "일기 작성 스타일 리스트 업데이트")
    @PutMapping("/user/style")
    fun updateStyleList(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @RequestBody styleList: List<String>
    ): List<String> =
        userStyleUseCase.updateStyleList(principal.id, styleList)

}