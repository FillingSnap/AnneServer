package com.anne.server.global.websocket.dto

import com.anne.server.global.websocket.WebSocketStatus
import kotlinx.serialization.Serializable

@Serializable
data class WebSocketResponseDto (

    val status: WebSocketStatus,

    val content: String?

)