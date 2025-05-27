package com.anne.server.global.logging.dto

data class CachedRequestData (

    val method: String,
    val uri: String,
    val clientIp: String,
    val headers: Map<String, String>,
    val requestParam: String

)