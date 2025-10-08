package com.anne.server.application.user.dto

import com.anne.server.domain.User

data class LoginInfo(

    val user: User,

    val accessToken: String,

    val refreshToken: String

)