package com.anne.server.domain_temp.diary.dto.response

import com.anne.server.domain_temp.diary.enums.SseStatus

data class SseResponse (

    val status: SseStatus,

    val content: String?

) {

}