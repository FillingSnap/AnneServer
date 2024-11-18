package com.anne.server.global.auth.fedCM

import com.anne.server.domain.user.dto.UserPayloadDto
import com.anne.server.global.auth.AuthServiceInterface
import com.anne.server.global.exception.CustomException
import com.anne.server.global.exception.ErrorCode
import com.anne.server.infra.google.GoogleService
import org.springframework.stereotype.Service

@Service
class FedCMService (

    private val googleService: GoogleService

): AuthServiceInterface {

    override fun getPayload(code: String, registrationId: String): UserPayloadDto {
        when (registrationId) {
            "google" -> return googleService.getPayload(code, registrationId)
            else -> throw CustomException(ErrorCode.WRONG_REGISTRATION_ID)
        }
    }

}