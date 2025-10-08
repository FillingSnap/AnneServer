package com.anne.server.common.exception

import org.springframework.http.HttpStatus

enum class ErrorCode (

    val status: HttpStatus,

    val message: String,

    val alert: Boolean = true

) {

    WRONG_REGISTRATION_ID(HttpStatus.UNAUTHORIZED, "잘못된 인증 제공자입니다"),
    GOOGLE_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "구글로부터 회원 정보를 받아올 수 없습니다"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다", false),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"),
    STORY_NOT_FOUND(HttpStatus.NOT_FOUND, "스토리를 찾을 수 없습니다"),
    DIARY_NOT_FOUND(HttpStatus.NOT_FOUND, "일기를 찾을 수 없습니다"),
    WRONG_URL(HttpStatus.NOT_FOUND, "잘못된 url 입니다", false),
    WRONG_PAGE(HttpStatus.NOT_FOUND, "잘못된 페이지 번호 입니다"),

    TOO_LARGE_MULTIFILE(HttpStatus.BAD_REQUEST, "업로드한 사진의 용량이 너무 큽니다"),
    TOO_MANY_STYLES(HttpStatus.BAD_REQUEST, "일기 스타일의 갯수가 너무 많습니다"),
    ALREADY_EXIST_UUID(HttpStatus.BAD_REQUEST, "이미 존재하는 UUID 입니다"),
    WRONG_IMAGE_EXTENSION(HttpStatus.BAD_REQUEST, "잘못된 확장자명입니다"),
    IMAGE_SAVE_ERROR(HttpStatus.BAD_REQUEST, "이미지 업로드 도중 문제가 발생했습니다"),
    IMAGE_TEXT_REQUIRED(HttpStatus.BAD_REQUEST, "사진과 설명은 최소 한 개 이상 존재해야 합니다"),
    IMAGE_TEXT_NOT_MATCHING(HttpStatus.BAD_REQUEST, "사진과 설명의 갯수가 일치하지 않습니다"),
    NOT_YOUR_STORY(HttpStatus.BAD_REQUEST, "해당 스토리의 소유자가 아닙니다"),
    NOT_YOUR_DIARY(HttpStatus.BAD_REQUEST, "해당 일기의 소유자가 아닙니다"),

    ALERT_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "알림 서버에 연결할 수 없습니다"),
    OBJECT_STORAGE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "오브젝트 스토리지에 연결할 수 없습니다"),
    IN_MEMORY_DB_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "인메모리 DB에 연결할 수 없습니다"),
    IS_DRAINING(HttpStatus.INTERNAL_SERVER_ERROR, "Connection Draining 진행 중입니다"),
    DIARY_GENERATE_UNAVAILABLE(HttpStatus.INTERNAL_SERVER_ERROR, "일기를 생성할 수 없습니다")
    ;

}