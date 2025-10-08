package com.anne.server.infrastructure.ai.adapter

import com.anne.server.application.diary.dto.DiaryStream
import com.anne.server.application.diary.dto.ImageTextDto
import com.anne.server.application.diary.port.out.DiaryStreamGenerator
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux

@Component
class DiaryStreamGeneratorAdapter(

    @Value("\${ai.url}")
    private val url: String,

    @Value("\${ai.test-url}")
    private val testUrl: String

): DiaryStreamGenerator {

    override fun diaryStream(imageTextList: List<ImageTextDto>): Flux<DiaryStream> =
        WebClient.create(url)
            .post()
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .bodyValue(imageTextList)
            .retrieve()
            .bodyToFlux(DiaryStream::class.java)
            .onErrorResume {
                Flux.just(DiaryStream(true, "AI server unavailable"))
            }

}