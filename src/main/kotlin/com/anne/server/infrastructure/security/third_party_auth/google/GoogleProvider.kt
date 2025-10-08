package com.anne.server.infrastructure.security.third_party_auth.google

import com.anne.server.application.user.dto.Payload
import com.anne.server.common.exception.ErrorCode
import com.anne.server.common.exception.CustomException
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Collections

@Component
class GoogleProvider(

    @Value("\${google.client-id}")
    private val clientId: String

) {

    private val transport = GoogleNetHttpTransport.newTrustedTransport()

    private val jsonFactory = GsonFactory.getDefaultInstance()

    fun getPayload(idToken: String): Payload {
        val verifier = GoogleIdTokenVerifier.Builder(transport, jsonFactory)
            .setAudience(Collections.singleton(clientId))
            .build()

        val payload = try {
            verifier.verify(idToken)
        } catch (_: Exception) {
            throw CustomException(ErrorCode.GOOGLE_UNAUTHORIZED)
        }.payload
            ?: throw CustomException(ErrorCode.GOOGLE_UNAUTHORIZED)

        return Payload(
            uid = payload.subject,
            name = payload["name"] as String
        )
    }

}