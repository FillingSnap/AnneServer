package com.anne.server.global.auth.oauth

import com.anne.server.domain.user.dto.UserPayloadDto
import com.anne.server.global.auth.AuthServiceInterface
import com.fasterxml.jackson.databind.JsonNode
import org.springframework.core.env.Environment
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

@Service
class OAuthService (

    private val env: Environment

): AuthServiceInterface {

    override fun getPayload(code: String, registrationId: String): UserPayloadDto {
        val accessToken = getAccessToken(code, registrationId)
        val userResourceNode = getUserResource(accessToken, registrationId)!!

        return UserPayloadDto(userResourceNode.get("id").asText(), userResourceNode.get("name").asText())
    }

    fun getAccessToken(code: String, registrationId: String): String {
        val clientId = env.getProperty("oauth2.$registrationId.client-id")
        val clientSecret = env.getProperty("oauth2.$registrationId.client-secret")
        val redirectUri = env.getProperty("oauth2.$registrationId.redirect-uri")
        val tokenUri = env.getProperty("oauth2.$registrationId.token-uri")

        val params = LinkedMultiValueMap<String, String>()
        params.add("code", code)
        params.add("client_id", clientId)
        params.add("client_secret", clientSecret)
        params.add("redirect_uri", redirectUri)
        params.add("grant_type", "authorization_code")

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val entity = HttpEntity(params, headers)

        val restTemplate = RestTemplate()
        restTemplate.errorHandler = OAuthErrorHandler()
        val response = restTemplate.exchange(tokenUri!!, HttpMethod.POST, entity, JsonNode::class.java)
        val accessTokenBody = response.body

        return accessTokenBody!!.get("access_token").asText()!!
    }

    fun getUserResource(accessToken: String, registrationId: String): JsonNode? {
        val restTemplate = RestTemplate()
        restTemplate.errorHandler = OAuthErrorHandler()
        val resourceUri = env.getProperty("oauth2.$registrationId.resource-uri")

        val headers = HttpHeaders()
        headers.set("Authorization", "Bearer $accessToken")

        val entity = HttpEntity(null, headers)
        return restTemplate.exchange(resourceUri!!, HttpMethod.GET, entity, JsonNode::class.java).body
    }

}