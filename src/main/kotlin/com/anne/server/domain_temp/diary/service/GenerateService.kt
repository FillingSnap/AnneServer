package com.anne.server.domain_temp.diary.service

import com.anne.server.domain_temp.diary.dao.DiaryRepository
import com.anne.server.domain_temp.diary.dto.response.SseResponse
import com.anne.server.domain_temp.diary.enums.SseStatus
import com.anne.server.domain_temp.story.dao.StoryRepository
import com.anne.server.domain_temp.story.service.StoryService
import com.anne.server.domain_temp.user.dto.UserDto
import com.anne.server.global.exception.enums.ErrorCode
import com.anne.server.global.registry.SseRegistry
import com.anne.server.infra.ai.dao.AiService
import com.anne.server.infra.discord.BotService
import com.anne.server.logger
import io.micrometer.context.ContextSnapshotFactory
import net.dv8tion.jda.api.EmbedBuilder
import org.slf4j.MDC
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.awt.Color
import java.time.Duration

@Service
class GenerateService (

    private val diaryRepository: DiaryRepository,

    private val storyRepository: StoryRepository,

    private val diaryService: DiaryService,

    private val storyService: StoryService,

    private val botService: BotService,

    private val aiService: AiService,

    private val sseRegistry: SseRegistry,

    private val contextSnapshotFactory: ContextSnapshotFactory

) {

    private val log = logger()

    fun test(delay: Long, uuid: String): SseEmitter {
        val emitter = sseRegistry.register(uuid, SseEmitter(5 * 60 * 1000))
        val sb = StringBuilder()
        aiService.test(delay)
            .publishOn(Schedulers.boundedElastic())
            .handle { sse, sink ->
                if (sse.error) {
                    sink.error(RuntimeException(sse.message))
                } else {
                    sink.next(sse.message)
                }
            }
            .doOnNext { response ->
                sb.append(response)
                try {
                    emitter.send(SseResponse(status = SseStatus.SUCCESS, content = response))
                } catch (_ : Exception) {}
            }
            .doOnError {
                val sseResponse = SseResponse(status = SseStatus.ERROR, content = ErrorCode.DIARY_GENERATE_UNAVAILABLE.message)
                try {
                    emitter.send(sseResponse)

                    Mono.delay(Duration.ofMillis(3000)).subscribe { emitter.complete() }
                } catch (_ : Exception) {}
            }
            .doOnComplete {
                val sseResponse = SseResponse(status = SseStatus.EOF, content = sb.toString())
                try {
                    emitter.send(sseResponse)

                    Mono.delay(Duration.ofMillis(3000)).subscribe { emitter.complete() }
                } catch (_ : Exception) {}
            }
            .withMdc(contextSnapshotFactory)
            .subscribe()

        return emitter
    }

    @Transactional
    fun generateDiary(delay: Long, uuid: String): SseEmitter {
        val emitter = sseRegistry.register(uuid, SseEmitter(5 * 60 * 1000))

        if (diaryRepository.existsDiaryByUuid(uuid)) {
            try {
                emitter.send(SseResponse(status = SseStatus.ERROR, content = ErrorCode.ALREADY_EXIST_UUID.message))

                Mono.delay(Duration.ofMillis(3000)).subscribe { emitter.complete() }
            } catch (_ : Exception) {}
            return emitter
        }

        if (!storyRepository.existsStoryByUuid(uuid)) {
            try {
                emitter.send(SseResponse(status = SseStatus.ERROR, content = ErrorCode.STORY_NOT_FOUND.message))

                Mono.delay(Duration.ofMillis(3000)).subscribe { emitter.complete() }
            } catch (_ : Exception) {}
            return emitter
        }

        val userDto = SecurityContextHolder.getContext().authentication.principal as UserDto
        val imageTextList = storyService.getImageAndTextByUuid(uuid)
        val sb = StringBuilder()
        val startTime = System.currentTimeMillis()

        aiService.generateDiary(imageTextList, delay)
            .publishOn(Schedulers.boundedElastic())
            .handle { sse, sink ->
                if (sse.error) {
                    sink.error(RuntimeException(sse.message))
                } else {
                    sink.next(sse.message)
                }
            }
            .doOnNext { response ->
                sb.append(response)
                try {
                    emitter.send(SseResponse(status = SseStatus.SUCCESS, content = response))
                } catch (_ : Exception) {}
                log.debug("SSE send: {}", response.take(256))
            }
            .doOnError { error ->
                val sseResponse = SseResponse(status = SseStatus.ERROR, content = ErrorCode.DIARY_GENERATE_UNAVAILABLE.message)
                try {
                    emitter.send(sseResponse)

                    Mono.delay(Duration.ofMillis(3000)).subscribe { emitter.complete() }
                } catch (_ : Exception) {}

                val elapsed = (System.currentTimeMillis() - startTime) / 1000.0
                log.error("""
                    |
                    |[SSE RESPONSE] ${"%.3f".format(elapsed)}s
                    |>> RESPONSE: SseResponse(status=${sseResponse.status}, content=${sseResponse.content?.take(1000)})
                """.trimMargin())
                botService.sendMessage("Error",
                    EmbedBuilder()
                        .setTitle("[SERVER LOG] Error Notification")
                        .setColor(Color.RED)
                        .addField("Request Id", MDC.get("requestId"), false)
                        .addField("Elapsed Time", "${elapsed}s", true)
                        .addField("SSE Status", sseResponse.status.toString(), false)
                        .addField("SSE Result", sseResponse.content.toString(), false)
                        .setTimestamp(java.time.OffsetDateTime.now())
                        .build()
                )
            }
            .doOnComplete {
                diaryService.saveDiary(userDto, sb.toString(), uuid)

                val sseResponse = SseResponse(status = SseStatus.EOF, content = sb.toString())
                try {
                    emitter.send(sseResponse)

                    Mono.delay(Duration.ofMillis(3000)).subscribe { emitter.complete() }
                } catch (_ : Exception) {}

                val elapsed = (System.currentTimeMillis() - startTime) / 1000.0
                log.info("""
                    |
                    |[SSE RESPONSE] ${"%.3f".format(elapsed)}s
                    |>> RESPONSE: SseResponse(status=${sseResponse.status}, content=${sseResponse.content?.take(1000)})
                """.trimMargin())
            }
            .withMdc(contextSnapshotFactory)
            .subscribe()

        return emitter
    }

    private fun <T> Flux<T>.withMdc(snapshotFactory: ContextSnapshotFactory): Flux<T> {
        val snap = snapshotFactory.captureAll()
        return this.contextWrite { ctx -> snap.updateContext(ctx) }
    }

}