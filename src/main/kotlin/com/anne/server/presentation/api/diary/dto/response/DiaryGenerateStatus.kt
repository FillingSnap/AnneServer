package com.anne.server.presentation.api.diary.dto.response

enum class DiaryGenerateStatus (

    val value: String

) {

    SUCCESS("SUCCESS"),
    ERROR("ERROR"),
    EOF("EOF")
    ;

}