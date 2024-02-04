package com.fillingsnap.server.global.config

import com.fillingsnap.server.domain.user.service.TokenService
import com.fillingsnap.server.domain.user.service.UserService
import com.fillingsnap.server.global.component.JwtAuthFilter
import com.fillingsnap.server.global.component.JwtAuthenticationEntryPoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
class SecurityConfig (

    private val tokenService: TokenService,

    private val userService: UserService

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
            it.requestMatchers("/user/test", "/error", "/login/oauth2/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
        }
        .addFilterBefore(JwtAuthFilter(tokenService, userService), UsernamePasswordAuthenticationFilter::class.java)
        .exceptionHandling {
            it.authenticationEntryPoint(JwtAuthenticationEntryPoint())
        }
        .build()!!

}