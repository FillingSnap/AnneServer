package com.anne.server.domain.user.dto.response

data class UserLoginResponseDto (

    val user: UserSimpleResponseDto,

    val accessToken: String,

    val refreshToken: String

)