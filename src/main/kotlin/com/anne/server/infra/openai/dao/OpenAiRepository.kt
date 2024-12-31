package com.anne.server.infra.openai.dao

import com.anne.server.global.exception.exceptions.CustomException
import com.anne.server.global.exception.enums.ErrorCode
import com.anne.server.infra.openai.config.JsonConfig
import com.anne.server.infra.openai.dto.ChatResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import java.time.Duration

@Component
class OpenAiRepository (

    @Value("\${openai.token}")
    private val token: String,

    @Value("\${openai.url}")
    private val url: String,

    private val jsonConfig: JsonConfig,

    private val restTemplate: RestTemplate

) {

    fun imageAnalyze(requestImage: String): String {
        // Http header & body
        val header = HttpHeaders()
        header.set("Authorization", "Bearer $token")

        val httpEntity = HttpEntity(
            jsonConfig.getImageAnalyzeRequestJson(requestImage),
            header
        )

        // Http Response
        val response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            httpEntity,
            ChatResponse::class.java
        )

        // ChatGPT Error
        if (response.statusCode != HttpStatus.OK) {
            throw CustomException(ErrorCode.CHAT_GPT_IMAGE_ANALYZE_FAILED)
        }

        return response.body!!.choices[0].message.content
    }

    fun generateDiary(question: String, delay: Long): Flux<String> {
        val client = WebClient.create(url)
        val eventStream = client.post()
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $token")
            .bodyValue(jsonConfig.getGenerateDiaryRequestJson(question))
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchangeToFlux { response ->
                response.bodyToFlux(String::class.java)
            }
            .delayElements(Duration.ofMillis(delay))

        return eventStream
    }

}