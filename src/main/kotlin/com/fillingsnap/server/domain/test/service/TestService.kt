package com.fillingsnap.server.domain.test.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fillingsnap.server.domain.test.vo.AnalysisResultVo
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.PropertySource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
@PropertySource("classpath:naver.yml")
class TestService(

    @Value("\${X-NCP-APIGW-API-KEY-ID}")
    private val keyId: String,

    @Value("\${X-NCP-APIGW-API-KEY}")
    private val key: String

) {

    fun sentimentAnalysis(plainText: String): AnalysisResultVo {
        val restTemplate = RestTemplate()
        restTemplate.requestFactory = HttpComponentsClientHttpRequestFactory()

        val url = "https://naveropenapi.apigw.ntruss.com/sentiment-analysis/v1/analyze"

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.set("X-NCP-APIGW-API-KEY-ID", keyId)
        headers.set("X-NCP-APIGW-API-KEY", key)

        val requestBody = "{\"content\": \"$plainText\"}"
        val requestEntity = HttpEntity(requestBody, headers)

        val responseEntity = restTemplate.postForEntity(url, requestEntity, String::class.java)
        val jsonResponse = responseEntity.body

        val objectMapper = ObjectMapper().registerModule(
            KotlinModule.Builder().build()
        )

        return objectMapper.readValue(jsonResponse, AnalysisResultVo::class.java)
    }

}