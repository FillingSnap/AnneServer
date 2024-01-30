package com.fillingsnap.server.domain.user.dto

data class UserLoginDto(

    val jwtToken: String,

    val refreshToken: String,

)