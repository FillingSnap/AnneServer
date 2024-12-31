package com.anne.server.domain.diary.dto.response

import com.anne.server.domain.diary.enums.SseStatus
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color

data class SseResponse (

    val status: SseStatus,

    val content: String?

) {

    fun toPrettierLog(): String {
        return """
        |
        |[REQUEST] POST /diary/generate ${this.status}
        |>> RESULT: ${this.content}
        """.trimIndent()
    }

    fun toPrettierEmbedMessage(): MessageEmbed {
        return EmbedBuilder()
            .setTitle("[SERVER LOG] Error Notification")
            .setColor(Color.RED)
            .addField("Status", "${this.status}", true)
            .addField("Content", "${this.content}", true)
            .build()
    }

}