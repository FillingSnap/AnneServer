package com.anne.server.global.exception

import org.springframework.http.HttpStatus

enum class ErrorCode (

    val status: HttpStatus,

    val message: String

) {

    GOOGLE_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "구글로부터 회원 정보를 받아올 수 없습니다"),
    INVALID_TOKEN(HttpStatus.FORBIDDEN, "유효하지 않은 토큰입니다"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"),
    STORY_NOT_FOUND(HttpStatus.NOT_FOUND, "스토리를 찾을 수 없습니다"),
    DIARY_NOT_FOUND(HttpStatus.NOT_FOUND, "일기를 찾을 수 없습니다"),
    TEMPORAL_DIARY_NOT_FOUND(HttpStatus.NOT_FOUND, "임시 저장된 일기를 찾을 수 없습니다"),
    WRONG_URL(HttpStatus.NOT_FOUND, "잘못된 url 입니다"),
    WRONG_PAGE(HttpStatus.NOT_FOUND, "잘못된 페이지 번호 입니다"),

    ALREADY_EXIST_UUID(HttpStatus.BAD_REQUEST, "이미 존재하는 UUID 입니다"),
    IMAGE_SAVE_ERROR(HttpStatus.BAD_REQUEST, "이미지 업로드 도중 문제가 발생했습니다"),
    IMAGE_TEXT_REQUIRED(HttpStatus.BAD_REQUEST, "사진과 설명은 최소 한 개 이상 존재해야 합니다"),
    IMAGE_TEXT_NOT_MATCHING(HttpStatus.BAD_REQUEST, "사진과 설명의 갯수가 일치하지 않습니다"),
    NOT_YOUR_STORY(HttpStatus.BAD_REQUEST, "해당 스토리의 소유자가 아닙니다"),
    NOT_YOUR_DIARY(HttpStatus.BAD_REQUEST, "해당 일기의 소유자가 아닙니다")
    ;

}