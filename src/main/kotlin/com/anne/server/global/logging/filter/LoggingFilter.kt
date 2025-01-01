package com.anne.server.global.logging.filter

import com.anne.server.global.logging.dto.CustomServerLog
import com.anne.server.infra.discord.BotService
import com.anne.server.logger
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper

@Component
class LoggingFilter (

    private val botService: BotService

): OncePerRequestFilter() {

    private final val log = logger()

    private final val exceptUri = listOf(
        "/swagger-ui/**", "/v3/api-docs/**", "/diary/generate"
    )

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val requestUri = request.requestURI
        if (exceptUri.any { AntPathMatcher().match(it, requestUri) }) {
            filterChain.doFilter(request, response)
            return
        }

        val cachingRequestWrapper = ContentCachingRequestWrapper(request)
        val cachingResponseWrapper = ContentCachingResponseWrapper(response)

        val startTime = System.currentTimeMillis()
        filterChain.doFilter(cachingRequestWrapper, cachingResponseWrapper)
        val end = System.currentTimeMillis()

        try {
            val logMessage = CustomServerLog.createInstance(
                requestWrapper = cachingRequestWrapper,
                responseWrapper = cachingResponseWrapper,
                elapsedTime = (end - startTime) / 1000.0
            )

            if (logMessage.httpStatus == HttpStatus.OK) {
                log.info(logMessage.toPrettierLog())
            } else {
                botService.sendMessage("Error", logMessage.toPrettierEmbedMessage())
                log.error(logMessage.toPrettierLog())
            }

            cachingResponseWrapper.copyBodyToResponse()
        } catch (e: Exception) {
            log.error("Logging 실패: ${e.message}")
        }
    }

}