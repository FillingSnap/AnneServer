package com.anne.server.global.exception.exceptions

import com.anne.server.global.exception.enums.ErrorCode

data class CustomException (

    val errorCode: ErrorCode

): Exception(errorCode.message)