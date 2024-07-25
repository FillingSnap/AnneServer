package com.anne.server.infra.openai.dto

data class SseResponseDto (

    val seq: Int,

    val status: SseStatus,

    val content: String?

) {
}