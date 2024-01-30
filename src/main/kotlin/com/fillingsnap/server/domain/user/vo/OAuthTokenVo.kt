package com.fillingsnap.server.domain.user.vo

data class OAuthTokenVo(

    val accessToken: String,

    val expiresIn: Int,

    val refreshToken: String,

    val scope: String,

    val tokenType: String,

    val idToken: String

)
