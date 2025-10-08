package com.anne.server.presentation.api.diary.controller

import com.anne.server.application.diary.port.`in`.DiaryGenerateUseCase
import com.anne.server.application.user.dto.AuthPrincipal
import com.anne.server.common.exception.CustomException
import com.anne.server.common.exception.ErrorCode
import com.anne.server.common.log.logger
import com.anne.server.common.reactor.withMdc
import com.anne.server.infrastructure.alert.adapter.JdaAdapter
import com.anne.server.infrastructure.lifecycle.DrainFlag
import com.anne.server.presentation.api.diary.dto.response.DiaryGenerateResponse
import com.anne.server.presentation.api.diary.dto.response.DiaryGenerateStatus
import com.anne.server.presentation.sse.LoggedSseEmitter
import com.anne.server.presentation.sse.SseRegistry
import io.micrometer.context.ContextSnapshotFactory
import io.swagger.v3.oas.annotations.Operation
import org.springframework.beans.factory.ObjectProvider
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Duration

@RestController
@RequestMapping("/api/diary")
class DiaryGenerateController(

    private val diaryGenerateUseCase: DiaryGenerateUseCase,

    private val sseRegistry: SseRegistry,

    private val drainFlag: DrainFlag,

    private val contextSnapshotFactory: ContextSnapshotFactory,

    // Prototype Scope Bean 생성 용도
    private val loggedSseEmitterProvider: ObjectProvider<SseEmitter>

) {

    @Operation(summary = "일기 생성")
    @PostMapping(
        value = ["/generate"],
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE]
    ) fun generateDiary(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @RequestParam delay: Long,
        @RequestBody uuid: String,
    ): SseEmitter {
        if (drainFlag.isDraining()) {
            throw CustomException(ErrorCode.IS_DRAINING)
        }

        val emitter = sseRegistry.register(
            uuid,
            loggedSseEmitterProvider.getObject()
        ) as LoggedSseEmitter

        val sb = StringBuilder()
        diaryGenerateUseCase.streamDiary(principal.id, uuid)
            .delayElements(Duration.ofMillis(delay))
            .publishOn(Schedulers.boundedElastic())
            .doOnNext {
                emitter.send(DiaryGenerateResponse(DiaryGenerateStatus.SUCCESS, it))
                sb.append(it)
            }
            .doOnError {
                emitter.failedWith(
                    DiaryGenerateResponse(
                        DiaryGenerateStatus.ERROR,
                        ErrorCode.DIARY_GENERATE_UNAVAILABLE.message
                    ), it
                )
            }
            .doOnComplete {
                val result = sb.toString()
                emitter.successWith(
                    DiaryGenerateResponse(
                        DiaryGenerateStatus.EOF,
                        result
                    )
                )
                diaryGenerateUseCase.generateDiary(principal.id, uuid, result)
            }
            .doFinally {
                Mono.delay(Duration.ofMillis(3000)).subscribe { emitter.complete() }
            }
            .withMdc(contextSnapshotFactory)
            .subscribe({ }, { }, { })

        return emitter
    }

}