package com.fillingsnap.server.domain.user.web

import com.fillingsnap.server.domain.user.dto.UserLoginDto
import com.fillingsnap.server.domain.user.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/user")
class UserController (

    private val userDetailsService: UserService

) {

    @GetMapping("/login")
    fun login(code: String): ResponseEntity<UserLoginDto> {
        return ResponseEntity.ok().body(userDetailsService.login(code))
    }

}