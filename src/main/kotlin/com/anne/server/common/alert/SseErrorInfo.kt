package com.anne.server.common.alert

data class SseErrorInfo(

    val requestId: String,

    val elapsedTime: Double,

    val result: String,

    val error: String?

)
