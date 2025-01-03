package com.anne.server.infra.discord

import jakarta.annotation.PreDestroy
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class BotService (

    @Value("\${discord.channel-id}")
    private val channelId: String,

    @Value("\${discord.bot.token}")
    private val token: String

) {

    private final val jda = JDABuilder.createDefault(token).build().awaitReady()

    fun sendMessage(title: String, messageEmbed: MessageEmbed) {
        try {
            val textChannel = jda.getTextChannelById(channelId)
            textChannel!!.sendMessage("").setEmbeds(messageEmbed).queue()
        } catch (e: Exception) {
            println(e)
        }
    }

    @PreDestroy
    fun shutdown() {
        jda.shutdownNow()
    }

}