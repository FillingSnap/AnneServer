package com.fillingsnap.server.global.component

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.MessageBuilder
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler

@Configuration
class StompErrorHandler: StompSubProtocolErrorHandler() {

    override fun handleClientMessageProcessingError(
        clientMessage: Message<ByteArray>?,
        ex: Throwable,
    ): Message<ByteArray>? {
        if (ex.message == "UNAUTHORIZED") {
            return errorMessage("유효하지 않은 권한입니다")
        }

        return super.handleClientMessageProcessingError(clientMessage, ex)
    }

    fun errorMessage(message: String): Message<ByteArray> {
        val accessor = StompHeaderAccessor.create(StompCommand.MESSAGE)
        accessor.message = message
        accessor.setLeaveMutable(true)

        return MessageBuilder.createMessage(message.toByteArray(), accessor.messageHeaders)
    }

}