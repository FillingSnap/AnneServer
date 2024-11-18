package com.anne.server.global.auth

import com.anne.server.domain.user.dto.UserPayloadDto
import com.anne.server.domain.user.dto.response.UserLoginResponseDto

interface AuthServiceInterface {

    fun getPayload(code: String, registrationId: String): UserPayloadDto

}