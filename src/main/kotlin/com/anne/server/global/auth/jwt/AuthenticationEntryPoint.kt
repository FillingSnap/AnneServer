package com.anne.server.global.auth.jwt

import com.fasterxml.jackson.databind.ObjectMapper
import com.anne.server.global.exception.ErrorCode
import com.anne.server.global.exception.dto.ExceptionResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class AuthenticationEntryPoint (

    private val objectMapper: ObjectMapper,

): AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        val error = ErrorCode.INVALID_TOKEN

        response.status = error.status.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "utf-8"
        response.writer.write(objectMapper.writeValueAsString(
            ExceptionResponse(
                status = error.status,
                requestUri = request.requestURI,
                data = error.message,
            )
        ))
    }

}