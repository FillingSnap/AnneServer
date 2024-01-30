package com.fillingsnap.server.global.component

import com.fillingsnap.server.domain.user.service.UserService
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.GenericFilterBean

@Component
class JwtFilter(

    private val userDetailsService: UserService

): GenericFilterBean() {

    override fun doFilter(request: ServletRequest, response: ServletResponse, filterChain: FilterChain) {
        val token: String? = (request as HttpServletRequest).getHeader("Authorization")
        val verifier = GoogleIdTokenVerifier(NetHttpTransport(), GsonFactory())

        if (token != null) {
            val split = token.split(" ")

            if (split.size == 2 && split[0] == "Bearer") {
                val jwtToken = verifier.verify(split[1])
                if (jwtToken != null) {
                    val userDetails = userDetailsService.loadUserByUsername(jwtToken.payload.subject)
                    SecurityContextHolder.getContext().authentication =
                        UsernamePasswordAuthenticationToken(userDetails, "", userDetails.authorities)
                }
                println(verifier.verify(split[1]))
            }
        }

        filterChain.doFilter(request, response)
    }

}