package com.anne.server.infrastructure.security.third_party_auth.adapter

import com.anne.server.application.user.dto.Payload
import com.anne.server.application.user.port.out.ThirdPartyAuthProvider
import com.anne.server.common.exception.ErrorCode
import com.anne.server.common.exception.CustomException
import com.anne.server.infrastructure.security.third_party_auth.google.GoogleProvider
import org.springframework.stereotype.Component

@Component
class ThirdPartyAuthProviderAdapter(

    private val googleProvider: GoogleProvider

): ThirdPartyAuthProvider {
    override fun getPayload(
        code: String,
        registrationId: String
    ): Payload =
        when (registrationId) {
            "google" -> googleProvider.getPayload(code)
            else -> throw CustomException(ErrorCode.WRONG_REGISTRATION_ID)
        }

}