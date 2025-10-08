package com.anne.server.application.user.port.`in`

import com.anne.server.application.user.dto.LoginInfo

interface LoginUseCase {

    fun login(code: String, registrationId: String): LoginInfo

}