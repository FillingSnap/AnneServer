package com.anne.server.domain.user.dto

data class LoginDto (

    val user: UserDto,

    val token: String,

    val refreshToken: String

)