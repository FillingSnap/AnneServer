package com.fillingsnap.server.global.component

import com.fillingsnap.server.domain.user.service.TokenService
import com.fillingsnap.server.domain.user.service.UserService
import com.fillingsnap.server.global.exception.CustomException
import com.fillingsnap.server.global.exception.ErrorCode
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.GenericFilterBean

@Component
class JwtAuthFilter(

    private val tokenService: TokenService,

    private val userService: UserService

): GenericFilterBean() {

    override fun doFilter(request: ServletRequest, response: ServletResponse, filterChain: FilterChain) {
        val token = (request as HttpServletRequest).getHeader("Authorization")

        if (token != null) {
            val split = token.split(" ")
            if (split.size == 2 && split[0] == "Bearer" && tokenService.verifyToken(split[1])) {
                val id: String = tokenService.getId(split[1])
                val user = userService.loadUserById(id.toLong()) ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

                val auth = UsernamePasswordAuthenticationToken(
                    user,
                    "",
                    emptyList()
                )
                SecurityContextHolder.getContext().authentication = auth
            }
        }

        filterChain.doFilter(request, response)
    }

}