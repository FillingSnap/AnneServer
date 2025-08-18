package com.anne.server.global.logging.filter

import com.anne.server.infra.discord.BotService
import com.anne.server.logger
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import net.dv8tion.jda.api.EmbedBuilder
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.awt.Color
import java.util.UUID

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class LoggingFilter (

    private val botService: BotService,

): OncePerRequestFilter() {

    private final val log = logger()

    private final val exceptUri = listOf(
        "/swagger-ui/**", "/v3/api-docs/**", "/internal/**", "/diary/generate", "/diary/generateTest"
    )

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val requestUri = request.requestURI

        val requestId = request.getHeader("X-Request-Id")
            ?.takeIf { it.isNotBlank() }
            ?: UUID.randomUUID().toString()
        val clientIp = request.getHeader("X-Forwarded-For")
            ?.split(",")?.firstOrNull()?.trim()
            ?: request.remoteAddr
        val headers = request.headerNames.toList()
            .associateWith { request.getHeader(it) }
            .toString()
        val params = request.parameterMap
            .map { (key, value) -> "$key=${value.joinToString()}" }
            .joinToString("&")

        MDC.put("requestId", requestId)
        response.setHeader("X-Request-Id", requestId)

        val requestWrapper = ContentCachingRequestWrapper(request)
        val responseWrapper = ContentCachingResponseWrapper(response)
        val requestMethod = request.method
        val requestBody = String(requestWrapper.contentAsByteArray)

        log.info("""
            |
            |[REQUEST] $requestMethod $requestUri
            |>> CLIENT_IP: $clientIp
            |>> HEADERS: $headers
            |>> REQUEST_PARAM: $params
            |>> REQUEST_BODY: $requestBody
        """.trimIndent())

        val startTime = System.currentTimeMillis()

        if (exceptUri.any { AntPathMatcher().match(it, requestUri) }) {
            filterChain.doFilter(request, response)
            return
        }

        filterChain.doFilter(requestWrapper, responseWrapper)

        val elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0
        val responseBody = String(responseWrapper.contentAsByteArray)
        val httpStatus = HttpStatus.valueOf(responseWrapper.status)
        if (httpStatus == HttpStatus.OK) {
            log.info("""
                |
                |[RESPONSE] ${responseWrapper.status} ${elapsedTime}s
                |>> RESPONSE_BODY: $responseBody
            """.trimIndent())
        } else {
            log.error("""
                |
                |[RESPONSE] ${responseWrapper.status} ${elapsedTime}s
                |>> RESPONSE_BODY: $responseBody
            """.trimIndent())
            botService.sendMessage("Error",
                EmbedBuilder()
                    .setTitle("[SERVER LOG] Error Notification")
                    .setColor(Color.RED)
                    .addField("Request Method & URI", "[$requestMethod] $requestUri", false)
                    .addField("HTTP Status", httpStatus.toString(), true)
                    .addField("Elapsed Time", "${elapsedTime}s", true)
                    .addField("Client IP", clientIp, false)
                    .addField("Headers", headers, false)
                    .addField("Request Params", params.take(1000), false)
                    .addField("Request Body", requestBody.take(500), false)
                    .addField("Response Body", responseBody.take(500), false)
                    .setTimestamp(java.time.OffsetDateTime.now())
                    .build()
            )
        }

        responseWrapper.copyBodyToResponse()
        MDC.clear()
    }

}