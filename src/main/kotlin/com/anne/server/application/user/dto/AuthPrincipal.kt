package com.anne.server.application.user.dto

data class AuthPrincipal(

    val id: Long,

    val provider: String,

    val uid: String

)
