package com.anne.server.global.logging.wrapper

import com.anne.server.domain.diary.dto.response.SseResponse
import com.anne.server.domain.diary.enums.SseStatus
import com.anne.server.infra.discord.BotService
import com.anne.server.logger
import net.dv8tion.jda.api.EmbedBuilder
import org.slf4j.MDC
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.awt.Color

class SseEmitterLoggingWrapper (

    private val botService: BotService,

): SseEmitter(5 * 60 * 1000) {

    private val log = logger()
    private val startTime = System.currentTimeMillis()
    private val mdcBase: Map<String, String>? = MDC.getCopyOfContextMap()

    private inline fun <T> withMdc(block: () -> T): T {
        val prev = MDC.getCopyOfContextMap()
        try {
            if (mdcBase != null) MDC.setContextMap(mdcBase) else MDC.clear()
            return block()
        } finally {
            if (prev != null) MDC.setContextMap(prev) else MDC.clear()
        }
    }

    override fun send(event: Any) = withMdc {
        val content = event.toString()
        try {
            super.send(SseResponse(status = SseStatus.SUCCESS, content = content))
        } catch (_: Throwable) {}
        log.debug("SSE send: {}", content.take(256))
    }

    fun complete(result: String) = withMdc {
        val sseResponse = SseResponse(status = SseStatus.EOF, content = result)
        try {
            super.send(sseResponse)
            super.complete()
        } catch (_: Throwable) {}
        val elapsed = (System.currentTimeMillis() - startTime) / 1000.0
        log.info("""
            |
            |[SSE RESPONSE] ${"%.3f".format(elapsed)}s
            |>> RESPONSE: SseResponse(status=${sseResponse.status}, content=${sseResponse.content?.take(1000)})
        """.trimMargin())
    }

    override fun completeWithError(ex: Throwable) = withMdc {
        val sseResponse = SseResponse(status = SseStatus.ERROR, content = ex.message ?: ex.javaClass.simpleName)
        try {
            super.send(sseResponse)
            super.complete()
        } catch (_: Throwable) {}
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

}