package com.fillingsnap.server.domain.user.service

import com.fasterxml.jackson.databind.JsonNode
import com.fillingsnap.server.domain.user.dao.UserRepository
import com.fillingsnap.server.domain.user.domain.User
import com.fillingsnap.server.domain.user.dto.TokenDto
import com.fillingsnap.server.global.config.security.OAuthErrorHandler
import org.springframework.core.env.Environment
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

@Service
class LoginService (

    private val env: Environment,

    private val userRepository: UserRepository,

    private val tokenService: TokenService

) {

    fun login(code: String, registrationId: String): TokenDto {
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

        return tokenService.generateToken(user.id!!.toString())
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

    // todo: 토큰 만료 시 refresh 로직 작성 필요

}