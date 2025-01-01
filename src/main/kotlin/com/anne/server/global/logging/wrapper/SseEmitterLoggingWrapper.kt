package com.anne.server.global.logging.wrapper

import com.anne.server.domain.diary.dto.response.SseResponse
import com.anne.server.domain.diary.enums.SseStatus
import com.anne.server.global.logging.dto.SseServerLog
import com.anne.server.infra.discord.BotService
import com.anne.server.logger
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

class SseEmitterLoggingWrapper (

    private val botService: BotService,

    private val request: HttpServletRequest

): SseEmitter(5 * 60 * 1000) {

    private val startTime = System.currentTimeMillis()

    private val log = logger()

    override fun send(event: Any) {
        val sseResponse = SseResponse(
            status = SseStatus.SUCCESS,
            content = event as String
        )
        return super.send(sseResponse)
    }

    fun complete(result: String) {
        val endTime = System.currentTimeMillis()

        val sseResponse = SseResponse(
            status = SseStatus.EOF,
            content = result
        )

        val sseLogMessage = SseServerLog.createInstance(
            request, (endTime - startTime) / 1000.0, sseResponse
        )

        log.info(sseLogMessage.toPrettierLog())
        super.send(sseResponse)
        super.complete()
    }

    override fun completeWithError(ex: Throwable) {
        val endTime = System.currentTimeMillis()

        val sseResponse = SseResponse(
            status = SseStatus.ERROR,
            content = ex.message
        )

        val sseLogMessage = SseServerLog.createInstance(
            request, (endTime - startTime) / 1000.0, sseResponse

        )

        log.error(sseLogMessage.toPrettierLog())
        botService.sendMessage("Error", sseLogMessage.toPrettierEmbedMessage())
        super.send(sseResponse)
        super.complete()
    }

}