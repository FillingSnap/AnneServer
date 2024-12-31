package com.anne.server.global.logging.dto

import com.anne.server.domain.diary.dto.response.SseResponse
import com.anne.server.domain.diary.enums.SseStatus
import jakarta.servlet.http.HttpServletRequest
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color

data class SseLogMessage (

    val httpMethod: String,

    val requestUri: String,

    val sseStatus: SseStatus,

    val clientIp: String,

    val elapsedTime: Double,

    val headers: String?,

    val requestParam: String?,

    val sseResult: String?

) {

    companion object {
        fun createInstance(
            request: HttpServletRequest,
            elapsedTime: Double,
            sseResponse: SseResponse
        ): SseLogMessage {
            return SseLogMessage(
                httpMethod = request.method,
                requestUri = request.requestURI,
                sseStatus = sseResponse.status,
                clientIp = getClientIpAddr(request),
                elapsedTime = elapsedTime,
                headers = request.headerNames.toList()
                    .associateWith { request.getHeader(it) }
                    .toString(),
                requestParam = request.parameterMap
                    .map { (key, value) -> "$key=${value.joinToString()}" }
                    .joinToString("&"),
                sseResult = sseResponse.content
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
    }

    fun toPrettierLog(): String {
        return """
        |
        |[REQUEST] ${this.httpMethod} ${this.requestUri} ${this.sseStatus} (${this.elapsedTime})
        |>> CLIENT_IP: ${this.clientIp}
        |>> HEADERS: ${this.headers}
        |>> REQUEST_PARAM: ${this.requestParam}
        |>> SSE RESULT: ${this.sseResult}
        """.trimIndent()
    }

    fun toPrettierEmbedMessage(): MessageEmbed {
        return EmbedBuilder()
            .setTitle("[SERVER LOG] Error Notification")
            .setColor(Color.RED)
            .addField("Request Method & URI", "[${this.httpMethod}] ${this.requestUri}", false)
            .addField("SSE Status", "${this.sseStatus}", true)
            .addField("Elapsed Time", "${this.elapsedTime}", true)
            .addField("Client IP", this.clientIp, false)
            .addField("Headers", this.headers ?: "null", false)
            .addField("Request Params", this.requestParam?.take(1000) ?: "null", false)
            .addField("SSE Result", this.sseResult.toString(), false)
            .setTimestamp(java.time.OffsetDateTime.now())
            .build()
    }

}