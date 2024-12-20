package com.anne.server.domain.user.dto.response

data class LoginResponse (

    val user: UserResponse,

    val accessToken: String,

    val refreshToken: String

)