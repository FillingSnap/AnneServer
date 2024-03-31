package com.fillingsnap.server.global.config.security

import com.fillingsnap.server.global.auth.jwt.JwtAuthenticationEntryPoint
import com.fillingsnap.server.global.auth.jwt.JwtAuthenticationFilter
import com.fillingsnap.server.global.auth.jwt.JwtAuthenticationService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter


@Configuration
class SecurityConfig (

    private val jwtAuthenticationService: JwtAuthenticationService

) {

    @Bean
    fun filterChain(http: HttpSecurity) = http
        .csrf {
            it.disable()
        }
        .sessionManagement {
            it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        }
        .authorizeHttpRequests {
            it.requestMatchers("/ws", "/diary/test", "/user/refresh", "/error", "/login/oauth2/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
        }
        .addFilterBefore(JwtAuthenticationFilter(jwtAuthenticationService), UsernamePasswordAuthenticationFilter::class.java)
        .exceptionHandling {
            it.authenticationEntryPoint(JwtAuthenticationEntryPoint())
        }
        .build()!!

}