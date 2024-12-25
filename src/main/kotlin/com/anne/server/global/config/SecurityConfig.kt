package com.anne.server.global.config

import com.anne.server.global.auth.jwt.AuthenticationEntryPoint
import com.anne.server.global.auth.jwt.AuthenticationFilter
import com.anne.server.global.auth.jwt.AuthenticationService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter


@Configuration
class SecurityConfig (

    private val authenticationService: AuthenticationService,

    private val authenticationEntryPoint: AuthenticationEntryPoint

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
            it.requestMatchers("/diary/test", "/user/token/refresh", "/error", "/login/oauth2/**", "/login/fedCM/**",
                "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
        }
        .addFilterBefore(AuthenticationFilter(authenticationService), UsernamePasswordAuthenticationFilter::class.java)
        .exceptionHandling {
            it.authenticationEntryPoint(authenticationEntryPoint)
        }
        .build()!!

}