package com.anne.server.global.logging.dto

import com.anne.server.domain.diary.dto.response.SseResponse
import com.anne.server.domain.diary.enums.SseStatus
import jakarta.servlet.http.HttpServletRequest
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color

data class SseServerLog (

    val httpMethod: String,

    val requestUri: String,

    val sseStatus: SseStatus,

    val clientIp: String,

    val elapsedTime: Double,

    val headers: String?,

    val requestParam: String?,

    val sseResult: String?

): ServerLog() {

    companion object {
        fun createInstance(
            request: HttpServletRequest,
            elapsedTime: Double,
            sseResponse: SseResponse
        ): SseServerLog {
            return SseServerLog(
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
    }

    override fun toPrettierLog(): String {
        return """
        |
        |[REQUEST] ${this.httpMethod} ${this.requestUri} ${this.sseStatus} (${this.elapsedTime})
        |>> CLIENT_IP: ${this.clientIp}
        |>> HEADERS: ${this.headers}
        |>> REQUEST_PARAM: ${this.requestParam}
        |>> SSE RESULT: ${this.sseResult}
        """.trimIndent()
    }

    override fun toPrettierEmbedMessage(): MessageEmbed {
        return EmbedBuilder()
            .setTitle("[SERVER LOG] Error Notification")
            .setColor(if (sseStatus == SseStatus.ERROR) Color.RED else Color.GREEN)
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