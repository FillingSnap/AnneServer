package com.anne.server.domain.user.dto.response

import com.anne.server.domain.user.dto.UserDto

data class LoginResponse (

    val user: UserDto,

    val accessToken: String,

    val refreshToken: String

)