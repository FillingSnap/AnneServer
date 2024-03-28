package com.fillingsnap.server.global.config.websocket

enum class WebSocketStatus (

    val value: String

) {

    UUID("UUID"),
    SUCCESS("SUCCESS"),
    ERROR("ERROR"),
    EOF("EOF")
    ;

}