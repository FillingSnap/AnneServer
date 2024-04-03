package com.fillingsnap.server.global.websocket.dto

import com.fillingsnap.server.global.websocket.WebSocketStatus
import kotlinx.serialization.Serializable

@Serializable
data class WebSocketResponseDto (

    val status: WebSocketStatus,

    val content: String?

)