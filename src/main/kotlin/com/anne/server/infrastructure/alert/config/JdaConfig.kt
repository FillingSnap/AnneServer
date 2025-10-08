package com.anne.server.infrastructure.alert.config

import com.anne.server.common.exception.CustomException
import com.anne.server.common.exception.ErrorCode
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.Executors

@Configuration
class JdaConfig(

    @Value("\${discord.bot.token}")
    private val token: String,

    @Value("\${discord.channel-id}")
    private val channelId: String

) {

    @Bean
    fun jda(): JDA = JDABuilder.createDefault(token)
        .setCallbackPool(Executors.newFixedThreadPool(4))
        .build()
        .awaitReady()

    @Bean
    fun textChannel(): TextChannel =
        jda().getTextChannelById(channelId)
            ?: throw CustomException(ErrorCode.ALERT_SERVER_ERROR)

}