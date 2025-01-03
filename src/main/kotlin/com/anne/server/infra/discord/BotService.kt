package com.anne.server.infra.discord

import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.concurrent.Executors

@Service
class BotService (

    @Value("\${discord.channel-id}")
    private val channelId: String,

    @Value("\${discord.bot.token}")
    private val token: String

) {

    private final val jda = JDABuilder.createDefault(token)
        .setCallbackPool(Executors.newFixedThreadPool(4))
        .build()
        .awaitReady()

    fun sendMessage(title: String, messageEmbed: MessageEmbed) {
        try {
            val textChannel = jda.getTextChannelById(channelId)
            textChannel!!.sendMessage("").setEmbeds(messageEmbed).queue()
        } catch (e: Exception) {
            println(e)
        }
    }

}