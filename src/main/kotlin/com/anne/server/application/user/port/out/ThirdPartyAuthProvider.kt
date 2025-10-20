package com.anne.server.application.user.port.out

import com.anne.server.application.user.dto.Payload

interface ThirdPartyAuthProvider {

    fun getPayload(code: String, registrationId: String): Payload

}