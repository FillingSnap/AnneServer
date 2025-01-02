package com.anne.server.infra.discord

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.MessageEmbed
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class BotService (

    @Value("\${discord.channel-id}")
    private val channelId: String,

    private val jda: JDA

) {

    fun sendMessage(title: String, messageEmbed: MessageEmbed) {
        try {
            val textChannel = jda.getTextChannelById(channelId)
            textChannel!!.sendMessage("").setEmbeds(messageEmbed).queue()
        } catch (e: Exception) {
            println(e)
        }
    }

}