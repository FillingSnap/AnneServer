package com.anne.server.global.logging.wrapper

import com.anne.server.domain.diary.dto.response.SseResponse
import com.anne.server.domain.diary.enums.SseStatus
import com.anne.server.global.logging.dto.CachedRequestData
import com.anne.server.infra.discord.BotService
import com.anne.server.logger
import jakarta.servlet.http.HttpServletRequest
import net.dv8tion.jda.api.EmbedBuilder
import org.slf4j.MDC
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import org.springframework.web.util.ContentCachingRequestWrapper
import java.awt.Color

class SseEmitterLoggingWrapper (

    private val botService: BotService,

    request: HttpServletRequest

): SseEmitter(5 * 60 * 1000) {

    private val startTime = System.currentTimeMillis()

    private val cachedRequestData: CachedRequestData

    private val mdcBase = MDC.getCopyOfContextMap()

    init {
        val wrapper = ContentCachingRequestWrapper(request)
        val headers = wrapper.headerNames.asSequence()
            .associateWith { wrapper.getHeader(it) ?: "" }
        val paramString = wrapper.parameterMap
            .map { (key, value) -> "$key=${value.joinToString()}" }
            .joinToString("&")

        cachedRequestData = CachedRequestData(
            method = wrapper.method,
            uri = wrapper.requestURI,
            clientIp = getClientIpAddr(wrapper),
            headers = headers,
            requestParam = paramString
        )
    }

    private fun getClientIpAddr(request: HttpServletRequest): String {
        var ip = request.getHeader("X-Forwarded-For")

        if (ip == null || ip.isEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("Proxy-Client-IP")
        }
        if (ip == null || ip.isEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("WL-Proxy-Client-IP")
        }
        if (ip == null || ip.isEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("HTTP_CLIENT_IP")
        }
        if (ip == null || ip.isEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR")
        }
        if (ip == null || ip.isEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.remoteAddr
        }

        return ip
    }

    private val log = logger()

    private inline fun <T> withMdc(block: () -> T): T {
        val prev = MDC.getCopyOfContextMap()
        try {
            if (mdcBase != null) MDC.setContextMap(mdcBase)
            return block()
        } finally {
            if (prev != null) MDC.setContextMap(prev)
        }
    }

    override fun send(event: Any) = withMdc {
        val sseResponse = SseResponse(
            status = SseStatus.SUCCESS,
            content = event as String
        )
        super.send(sseResponse)
    }

    fun complete(result: String) = withMdc {
        val endTime = System.currentTimeMillis()

        val sseResponse = SseResponse(
            status = SseStatus.EOF,
            content = result
        )

        val elapsedTime = (endTime - startTime) / 1000.0

        log.info("""
            |
            |[SSE RESPONSE] ${elapsedTime}s
            |>> RESPONSE: $sseResponse
        """.trimIndent())
        super.send(sseResponse)
        super.complete()
    }

    override fun completeWithError(ex: Throwable) = withMdc {
        val endTime = System.currentTimeMillis()

        val sseResponse = SseResponse(
            status = SseStatus.ERROR,
            content = ex.message
        )

        val elapsedTime = (endTime - startTime) / 1000.0

        log.error("""
            |
            |[SSE RESPONSE] ${elapsedTime}s
            |>> RESPONSE: $sseResponse
        """.trimIndent())
        botService.sendMessage("Error",
            EmbedBuilder()
                .setTitle("[SERVER LOG] Error Notification")
                .setColor(Color.RED)
                .addField("Request Method & URI", "[${cachedRequestData.method}] ${cachedRequestData.uri}", false)
                .addField("SSE Status", "${sseResponse.status}", true)
                .addField("Elapsed Time", "$elapsedTime", true)
                .addField("Client IP", cachedRequestData.clientIp, false)
                .addField("Headers", cachedRequestData.headers.toString(), false)
                .addField("Request Params", cachedRequestData.requestParam.take(1000), false)
                .addField("SSE Result", sseResponse.content.toString(), false)
                .setTimestamp(java.time.OffsetDateTime.now())
                .build()
        )
        super.send(sseResponse)
        super.complete()
    }

}