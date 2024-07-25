package com.anne.server.infra.openai.dto

enum class SseStatus (

    val value: String

) {

    SUCCESS("SUCCESS"),
    ERROR("ERROR"),
    EOF("EOF")
    ;

}