package com.anne.server.domain_temp.user.dto.response

import com.anne.server.domain_temp.user.dto.UserDto

data class LoginResponse (

    val user: UserDto,

    val accessToken: String,

    val refreshToken: String

)