package com.fillingsnap.server.global.config.websocket

import com.fillingsnap.server.global.config.security.JwtProvider
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageDeliveryException
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.stereotype.Component

@Component
class StompHandler (

    private val tokenService: JwtProvider

): ChannelInterceptor {

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
        val headerAccessor = StompHeaderAccessor.wrap(message)
        val type = headerAccessor.getHeader("stompCommand").toString()
        if (type == "CONNECT") {
            val token = headerAccessor.getNativeHeader("Authorization")
                ?: throw MessageDeliveryException("UNAUTHORIZED")

            // JWT 토큰 검증
            val split = token.toString().split(" ")
            if (!(split.size == 2 && tokenService.verifyToken(split[1]))) {
                throw MessageDeliveryException("UNAUTHORIZED")
            }

            // 세션에 저장
            val sessionAttributes = headerAccessor.sessionAttributes
            sessionAttributes!!["id"] = tokenService.getId(split[1])
            headerAccessor.sessionAttributes = sessionAttributes
        } else if (type == "SUBSCRIBE") {
            val destination = headerAccessor.destination
            val id = destination!!.substring("/queue/channel/".length)

            // 자신의 uid에 해당하는 채널을 구독하는지 확인
            if (id != headerAccessor.sessionAttributes!!["id"]) {
                throw MessageDeliveryException("UNAUTHORIZED")
            }
        } else if (type == "DISCONNECT") {
            println("DISCONNECTED")
        }

        return message
    }

}