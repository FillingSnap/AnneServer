package com.anne.server.infrastructure.alert.adapter

import com.anne.server.common.alert.ErrorInfo
import com.anne.server.common.alert.SseErrorInfo
import com.anne.server.infrastructure.alert.mapper.JdaMapper
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import org.springframework.stereotype.Component
import java.awt.Color

@Component
class JdaAdapter (

    private val textChannel: TextChannel

) {

    fun alertError(errorInfo: ErrorInfo) {
        try {
            textChannel.sendMessage("").setEmbeds(
                JdaMapper.toEmbed(
                    "[SERVER LOG] Error Notification",
                    Color.RED,
                    errorInfo
                )
            ).queue()
        } catch (e: Exception) {
            println(e)
        }
    }

    fun alertError(sseErrorInfo: SseErrorInfo) {
        try {
            textChannel.sendMessage("").setEmbeds(
                JdaMapper.toEmbed(
                    "[SERVER LOG] SSE Error Notification",
                    Color.RED,
                    sseErrorInfo
                )
            ).queue()
        } catch (e: Exception) {
            println(e)
        }
    }

}