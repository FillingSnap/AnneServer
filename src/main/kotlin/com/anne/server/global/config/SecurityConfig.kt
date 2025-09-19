package com.anne.server.global.config

import com.anne.server.global.auth.jwt.AuthenticationEntryPoint
import com.anne.server.global.auth.jwt.AuthenticationFilter
import com.anne.server.global.filter.InternalTokenFilter
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

    private val authenticationFilter: AuthenticationFilter,

    private val internalTokenFilter: InternalTokenFilter,

    private val authenticationEntryPoint: AuthenticationEntryPoint

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
        .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
        .exceptionHandling { it.authenticationEntryPoint(authenticationEntryPoint) }
        .build()!!

}