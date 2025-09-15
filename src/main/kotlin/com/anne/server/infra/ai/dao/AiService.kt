package com.anne.server.infra.ai.dao

import com.anne.server.infra.ai.dto.ImageTextDto
import com.anne.server.infra.ai.dto.ServerSentEvent
import io.micrometer.context.ContextSnapshotFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import java.time.Duration

@Component
class AiService (

    @Value("\${ai.url}")
    private val url: String,

    @Value("\${ai.test-url}")
    private val testUrl: String,

    private val contextSnapshotFactory: ContextSnapshotFactory

) {

    fun <T> Flux<T>.withMdc(snapshotFactory: ContextSnapshotFactory): Flux<T> {
        val snap = snapshotFactory.captureAll()
        return this.contextWrite { ctx -> snap.updateContext(ctx) }
    }

    fun generateDiary(imageTextList: List<ImageTextDto>, delay: Long): Flux<ServerSentEvent> {
        val client = WebClient.create(url)
        val eventStream = client.post()
            .header("Content-Type", "application/json")
            .bodyValue(imageTextList)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchangeToFlux { response ->
                response.bodyToFlux(ServerSentEvent::class.java)
            }
            .delayElements(Duration.ofMillis(delay))
            .withMdc(contextSnapshotFactory)

        return eventStream
    }

    fun test(delay: Long): Flux<ServerSentEvent> {
        val client = WebClient.create(testUrl)
        val eventStream = client.post()
            .header("Content-Type", "application/json")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchangeToFlux { response ->
                response.bodyToFlux(ServerSentEvent::class.java)
            }
            .delayElements(Duration.ofMillis(delay))
            .withMdc(contextSnapshotFactory)

        return eventStream
    }

}