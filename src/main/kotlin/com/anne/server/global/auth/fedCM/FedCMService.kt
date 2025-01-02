package com.anne.server.global.auth.fedCM

import com.anne.server.domain.user.dto.Payload
import com.anne.server.global.auth.AuthServiceInterface
import com.anne.server.global.exception.exceptions.CustomException
import com.anne.server.global.exception.enums.ErrorCode
import com.anne.server.infra.google.GoogleService
import org.springframework.stereotype.Service

@Service
class FedCMService (

    private val googleService: GoogleService

): AuthServiceInterface {

    override fun getPayload(code: String, registrationId: String): Payload {
        when (registrationId) {
            "google" -> return googleService.getPayload(code, registrationId)
            else -> throw CustomException(ErrorCode.WRONG_REGISTRATION_ID)
        }
    }

}