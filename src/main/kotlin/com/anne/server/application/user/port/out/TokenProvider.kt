package com.anne.server.application.user.port.out

import com.anne.server.application.user.dto.AuthPrincipal

interface TokenProvider {

    fun generateAccessToken(id: String, provider: String, uid: String): String

    fun validateAccessToken(token: String): AuthPrincipal?

    fun generateRefreshToken(id: String): String

    fun validateRefreshToken(token: String): String?

}