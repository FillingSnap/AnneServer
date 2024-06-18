package com.anne.server.infra.openai.dto

import com.anne.server.global.websocket.WebSocketStatus

data class SseResponseDto (

    val seq: Int,

    val status: WebSocketStatus,

    val content: String?

) {
}