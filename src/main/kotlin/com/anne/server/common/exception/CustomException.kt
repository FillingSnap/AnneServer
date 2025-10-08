package com.anne.server.common.exception

data class CustomException (

    val errorCode: ErrorCode

): Exception(errorCode.message)