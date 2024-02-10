package com.fillingsnap.server.global.exception

import org.springframework.http.HttpStatus

enum class ErrorCode (

    val status: HttpStatus,

    val message: String

) {

    GOOGLE_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "구글로부터 회원 정보를 받아올 수 없습니다"),
    INVALID_TOKEN(HttpStatus.FORBIDDEN, "유효하지 않은 토큰입니다"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"),
    STORY_NOT_FOUND(HttpStatus.NOT_FOUND, "스토리를 찾을 수 없습니다"),
    DIARY_NOT_FOUND(HttpStatus.NOT_FOUND, "일기를 찾을 수 없습니다")
    ;

}