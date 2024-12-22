package com.anne.server.domain.diary.dto.response

import com.anne.server.domain.diary.enums.SseStatus

data class SseResponse (

    val status: SseStatus,

    val content: String?

) {
}