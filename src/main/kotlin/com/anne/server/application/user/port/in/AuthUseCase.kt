package com.anne.server.application.user.port.`in`

import com.anne.server.application.user.dto.AuthPrincipal

interface AuthUseCase {

    fun getPrincipal(token: String): AuthPrincipal?

}