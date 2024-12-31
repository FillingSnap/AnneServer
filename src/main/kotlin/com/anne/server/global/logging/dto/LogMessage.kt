package com.anne.server.global.logging.dto

import jakarta.servlet.http.HttpServletRequest
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import org.springframework.http.HttpStatus
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.awt.Color

data class LogMessage (

    val httpMethod: String,

    val requestUri: String,

    val httpStatus: HttpStatus,

    val clientIp: String,

    val elapsedTime: Double,

    val headers: String?,

    val requestParam: String?,

    val requestBody: String?,

    val responseBody: String?

) {

    companion object {
        fun createInstance(
            requestWrapper: ContentCachingRequestWrapper,
            responseWrapper: ContentCachingResponseWrapper,
            elapsedTime: Double
        ): LogMessage {
            return LogMessage(
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
        |[REQUEST] ${this.httpMethod} ${this.requestUri} ${this.httpStatus} (${this.elapsedTime})
        |>> CLIENT_IP: ${this.clientIp}
        |>> HEADERS: ${this.headers}
        |>> REQUEST_PARAM: ${this.requestParam}
        |>> REQUEST_BODY: ${this.requestBody}
        |>> RESPONSE_BODY: ${this.responseBody}
        """.trimIndent()
    }

    fun toPrettierEmbedMessage(): MessageEmbed {
        return EmbedBuilder()
            .setTitle("[SERVER LOG] Error Notification")
            .setColor(Color.RED)
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