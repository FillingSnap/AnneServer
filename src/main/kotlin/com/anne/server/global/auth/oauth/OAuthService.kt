package com.anne.server.global.auth.oauth

import com.fasterxml.jackson.databind.JsonNode
import com.anne.server.domain.user.dao.UserRepository
import com.anne.server.domain.user.domain.User
import com.anne.server.domain.user.dto.response.UserLoginResponseDto
import com.anne.server.domain.user.dto.response.UserSimpleResponseDto
import com.anne.server.global.auth.jwt.JwtAuthenticationService
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

    private val env: Environment,

    private val userRepository: UserRepository,

    private val tokenService: JwtAuthenticationService

) {

    fun login(code: String, registrationId: String): UserLoginResponseDto {
        val accessToken = getAccessToken(code, registrationId)
        val userResourceNode = getUserResource(accessToken, registrationId)!!
        val uid = userResourceNode.get("id").asText()
        val nickname = userResourceNode.get("name").asText()

        var user = userRepository.findByUidAndProvider(uid, registrationId)
        if (user == null) {
           user = userRepository.save(
               User(
                   name = nickname,
                   uid = uid,
                   provider = registrationId
               )
           )
        }

        return UserLoginResponseDto(
            UserSimpleResponseDto(user),
            tokenService.generateAccessToken(user.id!!.toString(), user.provider, user.uid),
            tokenService.generateRefreshToken(user.id!!.toString())
        )
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