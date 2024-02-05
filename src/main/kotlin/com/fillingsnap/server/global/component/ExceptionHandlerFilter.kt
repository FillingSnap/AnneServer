package com.fillingsnap.server.global.component

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fillingsnap.server.global.exception.CustomException
import com.fillingsnap.server.global.exception.ErrorCode
import com.fillingsnap.server.global.exception.ExceptionResponseDto
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class ExceptionHandlerFilter: OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            filterChain.doFilter(request, response)
        } catch (e: CustomException) {
            val objectMapper = ObjectMapper()
            objectMapper.registerModule(JavaTimeModule())
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)

            val error = e.errorCode
            response.status = error.status.value()
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.characterEncoding = "utf-8"
            response.writer.write(objectMapper.writeValueAsString(
                ExceptionResponseDto(
                    status = error.status,
                    requestUri = request.requestURI,
                    data = error.message,
                )
            ))
        }
    }
}