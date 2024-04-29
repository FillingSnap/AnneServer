package com.anne.server.domain.user.dto.response

import com.anne.server.domain.user.dto.UserDto

data class UserLoginResponseDto (

    val user: UserDto,

    val accessToken: String,

    val refreshToken: String

)