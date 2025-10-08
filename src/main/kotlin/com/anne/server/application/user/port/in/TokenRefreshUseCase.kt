package com.anne.server.application.user.port.`in`

interface TokenRefreshUseCase {

    fun refreshAccessToken(refreshToken: String): String

    fun generateRefreshToken(id: Long): String

}