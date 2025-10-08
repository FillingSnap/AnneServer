package com.anne.server.infrastructure.alert.mapper

import com.anne.server.common.alert.ErrorInfo
import com.anne.server.common.alert.SseErrorInfo
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color
import java.time.OffsetDateTime

object JdaMapper {

    fun toEmbed(title: String, color: Color, errorInfo: ErrorInfo): MessageEmbed =
        EmbedBuilder()
            .setTitle(title)
            .setColor(color)
            .addField("Request Method & URI", "[${errorInfo.method}] ${errorInfo.requestUri}", false)
            .addField("Request Id", errorInfo.requestId, false)
            .addField("HTTP Status", errorInfo.httpStatus, true)
            .addField("Elapsed Time", "${errorInfo.elapsedTime}s", true)
            .addField("Client IP", errorInfo.clientIp, false)
            .addField("Headers", errorInfo.headers, false)
            .addField("Request Params", errorInfo.requestParams.take(1000), false)
            .addField("Request Body", errorInfo.requestBody.take(500), false)
            .addField("Response Body", errorInfo.responseBody.take(500), false)
            .setTimestamp(OffsetDateTime.now())
            .build()

    fun toEmbed(title: String, color: Color, sseErrorInfo: SseErrorInfo) =
        EmbedBuilder()
            .setTitle(title)
            .setColor(color)
            .addField("Request Id", sseErrorInfo.requestId, false)
            .addField("Elapsed Time", "${sseErrorInfo.elapsedTime}s", true)
            .addField("SSE Result", sseErrorInfo.result, false)
            .addField("Error", sseErrorInfo.error ?: "NULL", false)
            .setTimestamp(OffsetDateTime.now())
            .build()

}