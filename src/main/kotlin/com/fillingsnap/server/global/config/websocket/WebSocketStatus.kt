package com.fillingsnap.server.global.config.websocket

enum class WebSocketStatus (

    val value: String

) {

    SUCCESS("SUCCESS"),
    ERROR("ERROR"),
    EOF("EOF")
    ;

}