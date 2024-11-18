package com.anne.server.infra.google

import com.anne.server.domain.user.dto.UserPayloadDto
import com.anne.server.global.exception.CustomException
import com.anne.server.global.exception.ErrorCode
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
class GoogleService (

    @Value("\${oauth2.google.client-id}")
    private val clientId: String

) {

    private val transport = GoogleNetHttpTransport.newTrustedTransport()

    private val jsonFactory = GsonFactory.getDefaultInstance()

    fun getPayload(idToken: String, registrationId: String): UserPayloadDto {
        val verifier = GoogleIdTokenVerifier.Builder(transport, jsonFactory)
            .setAudience(Collections.singleton(clientId))
            .build()

        val token = try {
            verifier.verify(idToken)
        } catch (e: Exception) {
            throw CustomException(ErrorCode.GOOGLE_UNAUTHORIZED)
        }

        if (token == null) {
            throw CustomException(ErrorCode.GOOGLE_UNAUTHORIZED)
        } else {
            val payload = token.payload

            return UserPayloadDto(payload.subject, payload["name"] as String)
        }
    }

}