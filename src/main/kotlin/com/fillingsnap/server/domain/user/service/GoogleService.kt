package com.fillingsnap.server.domain.user.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fillingsnap.server.domain.user.vo.OAuthTokenVo
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.client.RestTemplate

@Service
@RequestMapping("/user")
class GoogleService {

    fun googleOAuthLogin(code: String): OAuthTokenVo {
        val restTemplate = RestTemplate()
        restTemplate.requestFactory = HttpComponentsClientHttpRequestFactory()

        val url = "https://oauth2.googleapis.com/token"

        val headers = HttpHeaders()
        val requestBody = "{\n" +
                "    \"client_id\": \"726447316352-e66kvk2j6g48dq9boocqrk9338qdblan.apps.googleusercontent.com\", \n" +
                "    \"client_secret\": \"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\", \n" +
                "    \"code\": \"${code}\", \n" +
                "    \"grant_type\": \"authorization_code\", \n" +
                "    \"redirect_uri\": \"http://localhost:8080/auth/google\"\n" +
                "}"
        val requestEntity = HttpEntity(requestBody, headers)
        val responseEntity: ResponseEntity<String> = restTemplate.postForEntity(url, requestEntity, String::class.java)

        val jsonResponse = responseEntity.body
        val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
        objectMapper.propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE

        // todo: 예외처리(잘못된 코드를 보냈을 경우)
        return objectMapper.readValue(jsonResponse, OAuthTokenVo::class.java)!!
    }

}