package com.anne.server.global.exception.dto

import org.springframework.http.HttpStatus
import java.time.LocalDateTime

data class ExceptionResponse<T> (

    val time: LocalDateTime = LocalDateTime.now(),

    val status: HttpStatus,

    val requestUri: String,

    val data: T

)