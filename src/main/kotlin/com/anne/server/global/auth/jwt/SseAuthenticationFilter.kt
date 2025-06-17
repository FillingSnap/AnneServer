package com.anne.server.global.auth.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class SseAuthenticationFilter (

    private val authenticationService: AuthenticationService

): OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val acceptHeader = request.getHeader(HttpHeaders.ACCEPT)

        if (acceptHeader != null && acceptHeader.contains("text/event-stream")) {
            val token = request.getHeader("Authorization")

            if (token != null) {
                val split = token.split(" ")
                if (split.size == 2 && split[0] == "Bearer" && authenticationService.verifyToken(split[1])) {
                    val auth = authenticationService.getAuthentication(split[1])
                    SecurityContextHolder.getContext().authentication = auth
                }
            }
        }

        filterChain.doFilter(request, response)
    }

}