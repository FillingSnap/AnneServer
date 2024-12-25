package com.anne.server.global.auth

import com.anne.server.domain.user.dto.Payload

interface AuthServiceInterface {

    fun getPayload(code: String, registrationId: String): Payload

}