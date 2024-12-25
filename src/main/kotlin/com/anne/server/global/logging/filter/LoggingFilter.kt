package com.anne.server.global.logging.filter

import com.anne.server.global.logging.dto.LogMessage
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
class LoggingFilter: OncePerRequestFilter() {

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
            val logMessage = LogMessage.createInstance(
                requestWrapper = cachingRequestWrapper,
                responseWrapper = cachingResponseWrapper,
                elapsedTime = (end - startTime) / 1000.0
            )

            if (logMessage.httpStatus == HttpStatus.OK) {
                log.info(logMessage.toPrettierLog())
            } else {
                log.error(logMessage.toPrettierLog())
            }

            cachingResponseWrapper.copyBodyToResponse()
        } catch (e: Exception) {
            log.error("[${this::class.simpleName}] Logging 실패")
        }
    }

}