package com.anne.server.presentation.security.logging

import com.anne.server.common.alert.ErrorInfo
import com.anne.server.common.log.logger
import com.anne.server.infrastructure.alert.adapter.JdaAdapter
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.util.UUID


@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class LoggingFilter (

    private val jdaAdapter: JdaAdapter

): OncePerRequestFilter() {

    private final val log = logger()

    private final val exceptUri = listOf(
        "/swagger-ui/**", "/v3/api-docs/**", "/api/internal/**"
    )

    private final val sseUri = listOf(
        "/api/diary/generate", "/api/diary/generateTest"
    )

    private val attrReqId = "mdc.reqId"

    override fun shouldNotFilterAsyncDispatch() = false

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

        val isFirst = !isAsyncDispatch(request)
        val isSse = sseUri.any { AntPathMatcher().match(it, requestUri) }

        if (isFirst) {
            val requestId = request.getHeader("X-Request-Id")?.takeIf { it.isNotBlank() }
                ?: UUID.randomUUID().toString()
            request.setAttribute(attrReqId, requestId)
            response.setHeader("X-Request-Id", requestId)
        }

        (request.getAttribute(attrReqId) as? String)?.let { MDC.put("requestId", it) }

        val method = request.method
        val clientIp = request.getHeader("X-Forwarded-For")?.split(",")?.firstOrNull()?.trim() ?: request.remoteAddr
        val headers = request.headerNames.toList().associateWith { request.getHeader(it) }.toString()
        val params = request.parameterMap.entries.joinToString("&") { (k, v) -> "$k=${v.joinToString()}" }
        val requestWrapper = ContentCachingRequestWrapper(request)
        val requestBody = String(requestWrapper.contentAsByteArray)
        if (isFirst) {
            log.info("""
                |
                |[REQUEST] $method $requestUri
                |>> CLIENT_IP: $clientIp
                |>> HEADERS: $headers
                |>> REQUEST_PARAM: $params
                |>> REQUEST_BODY: $requestBody
            """.trimMargin())
        }

        val responseWrapper = if (isSse) null else ContentCachingResponseWrapper(response)
        try {
            if (isSse) {
                filterChain.doFilter(requestWrapper, response)
            } else {
                val startTime = System.currentTimeMillis()
                filterChain.doFilter(requestWrapper, responseWrapper)
                val elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0

                val httpStatus = HttpStatus.valueOf(responseWrapper!!.status)
                val responseBody = String(responseWrapper.contentAsByteArray)

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
                    val alert = MDC.get("alert")
                    if (alert == null || alert.toBoolean()) {
                        jdaAdapter.alertError(
                            ErrorInfo(
                                method = method,
                                requestUri = request.requestURI,
                                requestId = request.getAttribute(attrReqId) as String,
                                httpStatus = httpStatus.toString(),
                                elapsedTime = elapsedTime,
                                clientIp = clientIp,
                                headers = headers,
                                requestParams = params,
                                requestBody = requestBody,
                                responseBody = responseBody
                            )
                        )
                    }
                }
            }
        } finally {
            responseWrapper?.copyBodyToResponse()
            if (!request.isAsyncStarted) {
                MDC.clear()
            }
        }
    }

}