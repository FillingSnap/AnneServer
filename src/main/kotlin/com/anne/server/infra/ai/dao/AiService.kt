package com.anne.server.infra.ai.dao

import com.anne.server.infra.ai.dto.ImageTextDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import java.time.Duration

@Component
class AiService (

    @Value("\${ai.url}")
    private val url: String

) {

    fun generateDiary(imageTextList: List<ImageTextDto>, delay: Long): Flux<String> {
        val client = WebClient.create(url)
        val eventStream = client.post()
            .header("Content-Type", "application/json")
            .bodyValue(imageTextList)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchangeToFlux { response ->
                response.bodyToFlux(String::class.java)
            }
            .delayElements(Duration.ofMillis(delay))

        return eventStream
    }

}