package com.anne.server.global.logging.dto

import jakarta.servlet.http.HttpServletRequest
import net.dv8tion.jda.api.entities.MessageEmbed

abstract class ServerLog {

    abstract fun toPrettierLog(): String

    abstract fun toPrettierEmbedMessage(): MessageEmbed

    companion object {
        fun getClientIpAddr(request: HttpServletRequest): String {
            var ip = request.getHeader("X-Forwarded-For")

            if (ip == null || ip.isEmpty() || "unknown".equals(ip, ignoreCase = true)) {
                ip = request.getHeader("Proxy-Client-IP")
            }
            if (ip == null || ip.isEmpty() || "unknown".equals(ip, ignoreCase = true)) {
                ip = request.getHeader("WL-Proxy-Client-IP")
            }
            if (ip == null || ip.isEmpty() || "unknown".equals(ip, ignoreCase = true)) {
                ip = request.getHeader("HTTP_CLIENT_IP")
            }
            if (ip == null || ip.isEmpty() || "unknown".equals(ip, ignoreCase = true)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR")
            }
            if (ip == null || ip.isEmpty() || "unknown".equals(ip, ignoreCase = true)) {
                ip = request.remoteAddr
            }

            return ip
        }
    }

}