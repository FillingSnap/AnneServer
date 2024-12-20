package com.anne.server.infra.openai.dto

data class SseResponse (

    val status: SseStatus,

    val content: String?

) {
}