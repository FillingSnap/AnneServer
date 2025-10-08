package com.anne.server.common.alert

data class ErrorInfo(

    val method: String,

    val requestUri: String,

    val requestId: String,

    val httpStatus: String,

    val elapsedTime: Double,

    val clientIp: String,

    val headers: String,

    val requestParams: String,

    val requestBody: String,

    val responseBody: String

)