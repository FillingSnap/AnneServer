package com.anne.server.presentation.security.auth

import com.anne.server.application.user.port.`in`.AuthUseCase
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class AuthFilter(

    private val authUseCase: AuthUseCase

): OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val context = SecurityContextHolder.getContext()
        AuthHeaderUtil.extractBearer(request.getHeader("Authorization"))
            ?.let {
                authUseCase.getPrincipal(it)
            }
            ?.let {
            context.authentication = UsernamePasswordAuthenticationToken(
                it, null, emptyList()
            )
        }

        filterChain.doFilter(request, response)
    }

}