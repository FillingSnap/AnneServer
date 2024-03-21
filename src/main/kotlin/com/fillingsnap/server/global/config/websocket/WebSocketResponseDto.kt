package com.fillingsnap.server.global.config.websocket

import kotlinx.serialization.Serializable

@Serializable
data class WebSocketResponseDto (

    val status: WebSocketStatus,

    val content: String?

)