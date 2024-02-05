package com.fillingsnap.server.global.component

import com.fillingsnap.server.global.exception.CustomException
import com.fillingsnap.server.global.exception.ErrorCode
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.stereotype.Component

@Component
class StompHandler: ChannelInterceptor {

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        val headerAccessor = StompHeaderAccessor.wrap(message)
        val type = headerAccessor.getHeader("stompCommand").toString()
        if (type == "CONNECT") {
            // todo: JWT 토큰 체크 후, 에러 처리 필요
            val token = headerAccessor.getNativeHeader("Authorization")

            if (token == null) {
                println("1")
                throw CustomException(ErrorCode.INVALID_TOKEN)
            }

            val split = token.toString().split(" ")

            if (split.size != 2) {
                println("2")
                throw CustomException(ErrorCode.INVALID_TOKEN)
            }
        }

        return message
    }

}