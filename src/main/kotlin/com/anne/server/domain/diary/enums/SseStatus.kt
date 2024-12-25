package com.anne.server.domain.diary.enums

enum class SseStatus (

    val value: String

) {

    SUCCESS("SUCCESS"),
    ERROR("ERROR"),
    EOF("EOF")
    ;

}