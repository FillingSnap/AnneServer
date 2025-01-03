package com.anne.server.infra.discord

import com.anne.server.logger
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Configuration
class BotConfig (

    @Value("\${discord.bot.token}")
    private val token: String

) {

    private val log = logger()

    @Bean
    fun executorService(): ExecutorService {
        return Executors.newFixedThreadPool(8)
    }

    @Bean
    fun jda(): JDA {
        log.info("JDA Build Start")
        return JDABuilder.createDefault(token)
            .setRateLimitElastic(executorService())
            .build()
    }

}