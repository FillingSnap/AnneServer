package com.anne.server.global.config

import io.micrometer.context.ContextRegistry
import io.micrometer.context.ContextSnapshotFactory
import io.micrometer.context.integration.Slf4jThreadLocalAccessor
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Hooks

@Configuration
class ContextPropagationConfig {

    @Bean
    fun contextSnapshotFactory(): ContextSnapshotFactory =
        ContextSnapshotFactory.builder().build()

    @Bean
    fun contextPropagationInit() = ApplicationRunner {
        ContextRegistry.getInstance().registerThreadLocalAccessor(Slf4jThreadLocalAccessor())
        Hooks.enableAutomaticContextPropagation()
    }

}