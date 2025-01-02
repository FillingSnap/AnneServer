package com.anne.server.infra.discord

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BotConfig (

    @Value("\${discord.bot.token}")
    private val token: String

) {

    private final var jda: JDA? = null

    @Bean
    fun jda(): JDA {
        if (jda == null) {
            jda = JDABuilder.createDefault(token).build()
        }
        return jda!!
    }

}