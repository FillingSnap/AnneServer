package com.anne.server.domain.user.web

import com.anne.server.domain.user.service.UserService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/user")
class UserController (

    private val userService: UserService

) {

    @Operation(summary = "일기 작성 스타일 리스트 조회")
    @GetMapping("/style")
    fun getStyleList(): ResponseEntity<List<String>> {
        return ResponseEntity.ok().body(userService.getStyleList())
    }

    @Operation(summary = "일기 작성 스타일 리스트 업데이트")
    @PutMapping("/style")
    fun updateStyleList(@RequestBody styleList: List<String>): ResponseEntity<Unit> {
        return ResponseEntity.ok().body(userService.updateStyleList(styleList))
    }

}