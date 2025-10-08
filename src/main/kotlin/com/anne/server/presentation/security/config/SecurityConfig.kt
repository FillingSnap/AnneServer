package com.anne.server.presentation.security.config

import com.anne.server.presentation.api.internal.filter.InternalTokenFilter
import com.anne.server.presentation.security.auth.AuthEntryPoint
import com.anne.server.presentation.security.auth.AuthFilter
import jakarta.servlet.DispatcherType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.firewall.HttpFirewall
import org.springframework.security.web.firewall.StrictHttpFirewall

@Configuration
class SecurityConfig (

    private val authFilter: AuthFilter,

    private val internalTokenFilter: InternalTokenFilter,

    private val authEntryPoint: AuthEntryPoint

) {

    @Bean
    fun httpFirewall(): HttpFirewall {
        return StrictHttpFirewall().apply {
            setAllowSemicolon(true)
            setAllowUrlEncodedSlash(true)
            setAllowUrlEncodedDoubleSlash(true)
        }
    }

    @Bean
    fun webSecurityCustomizer(firewall: HttpFirewall) = WebSecurityCustomizer {
        it.httpFirewall(firewall)
    }

    @Bean
    fun filterChain(http: HttpSecurity) = http
        .securityMatcher("/api/**")
        .csrf { it.disable() }
        .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        .authorizeHttpRequests {
            it.dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll()
                .requestMatchers(
                    "/api/healthCheck",
                    "/api/internal/**",
                    "/api/user/token/refresh",
                    "/api/login/fedCM/**"
                ).permitAll()
                .anyRequest().authenticated()
        }
        .addFilterBefore(internalTokenFilter, UsernamePasswordAuthenticationFilter::class.java)
        .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter::class.java)
        .exceptionHandling { it.authenticationEntryPoint(authEntryPoint) }
        .build()!!

}