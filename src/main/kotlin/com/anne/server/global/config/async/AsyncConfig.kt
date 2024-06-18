package com.anne.server.global.config.async

import org.springframework.context.annotation.Configuration
import org.springframework.core.task.AsyncTaskExecutor
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
@EnableAsync
class AsyncConfig: WebMvcConfigurer {

    override fun configureAsyncSupport(configurer: AsyncSupportConfigurer) {
        configurer.setTaskExecutor(asyncExecutor())
    }

    private fun asyncExecutor(): AsyncTaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.initialize()
        return executor
    }

}