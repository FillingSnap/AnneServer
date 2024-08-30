package com.anne.server.infra.openai.dto

data class SseResponseDto (

    val status: SseStatus,

    val content: String?

) {
}