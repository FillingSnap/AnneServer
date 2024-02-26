package com.fillingsnap.server.global.config.security

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.GenericFilterBean

class JwtAuthenticationFilter(

    private val jwtProvider: JwtProvider

): GenericFilterBean() {

    override fun doFilter(request: ServletRequest, response: ServletResponse, filterChain: FilterChain) {
        val token = (request as HttpServletRequest).getHeader("Authorization")

        if (token != null) {
            val split = token.split(" ")
            if (split.size == 2 && split[0] == "Bearer" && jwtProvider.verifyToken(split[1])) {
                val auth = jwtProvider.getAuthentication(split[1])
                SecurityContextHolder.getContext().authentication = auth
            }
        }

        filterChain.doFilter(request, response)
    }

}