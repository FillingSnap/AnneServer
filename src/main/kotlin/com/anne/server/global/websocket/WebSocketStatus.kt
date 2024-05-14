package com.anne.server.global.websocket

enum class WebSocketStatus (

    val value: String

) {

    UUID("UUID"),
    SUCCESS("SUCCESS"),
    ERROR("ERROR"),
    EOF("EOF")
    ;

}