package com.fillingsnap.server.global.exception

data class CustomException (

    val errorCode: ErrorCode

): Exception()