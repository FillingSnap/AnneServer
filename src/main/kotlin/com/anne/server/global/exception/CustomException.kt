package com.anne.server.global.exception

data class CustomException (

    val errorCode: ErrorCode

): Exception()