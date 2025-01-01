package com.anne.server.global.logging.dto

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import org.springframework.http.HttpStatus
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.awt.Color

data class CustomServerLog (

    val httpMethod: String,

    val requestUri: String,

    val httpStatus: HttpStatus,

    val clientIp: String,

    val elapsedTime: Double,

    val headers: String?,

    val requestParam: String?,

    val requestBody: String?,

    val responseBody: String?

): ServerLog() {

    companion object {
        fun createInstance(
            requestWrapper: ContentCachingRequestWrapper,
            responseWrapper: ContentCachingResponseWrapper,
            elapsedTime: Double
        ): CustomServerLog {
            return CustomServerLog(
                httpMethod = requestWrapper.method,
                requestUri = requestWrapper.requestURI,
                httpStatus = HttpStatus.valueOf(responseWrapper.status),
                clientIp = getClientIpAddr(requestWrapper),
                elapsedTime = elapsedTime,
                headers = requestWrapper.headerNames.toList()
                    .associateWith { requestWrapper.getHeader(it) }
                    .toString(),
                requestParam = requestWrapper.parameterMap
                    .map { (key, value) -> "$key=${value.joinToString()}" }
                    .joinToString("&"),
                requestBody = String(requestWrapper.contentAsByteArray),
                responseBody = String(responseWrapper.contentAsByteArray)
            )
        }
    }

    override fun toPrettierLog(): String {
        return """
        |
        |[REQUEST] ${this.httpMethod} ${this.requestUri} ${this.httpStatus} (${this.elapsedTime})
        |>> CLIENT_IP: ${this.clientIp}
        |>> HEADERS: ${this.headers}
        |>> REQUEST_PARAM: ${this.requestParam}
        |>> REQUEST_BODY: ${this.requestBody}
        |>> RESPONSE_BODY: ${this.responseBody}
        """.trimIndent()
    }

    override fun toPrettierEmbedMessage(): MessageEmbed {
        return EmbedBuilder()
            .setTitle("[SERVER LOG] Error Notification")
            .setColor(if (this.httpStatus == HttpStatus.OK) Color.GREEN else Color.RED)
            .addField("Request Method & URI", "[${this.httpMethod}] ${this.requestUri}", false)
            .addField("HTTP Status", this.httpStatus.toString(), true)
            .addField("Elapsed Time", "${this.elapsedTime}ms", true)
            .addField("Client IP", this.clientIp, false)
            .addField("Headers", this.headers ?: "null", false)
            .addField("Request Params", this.requestParam?.take(1000) ?: "null", false)
            .addField("Request Body", this.requestBody?.take(500) ?: "null", false)
            .addField("Response Body", this.responseBody?.take(500) ?: "null", false)
            .setTimestamp(java.time.OffsetDateTime.now())
            .build()
    }

}