package com.anne.server.presentation.sse

import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.LongAdder

@Component
class SseRegistry {

    private val emitterMap = ConcurrentHashMap<String, SseEmitter>()
    private val openCount = LongAdder()

    fun register(key: String, emitter: SseEmitter): SseEmitter {
        emitterMap[key] = emitter
        openCount.increment()

        emitter.onCompletion {
            emitterMap.remove(key)
            openCount.decrement()
        }

        emitter.onTimeout {
            emitterMap.remove(key)
            openCount.decrement()
        }

        emitter.onError {
            _ -> {
                emitterMap.remove(key)
                openCount.decrement()
            }
        }

        return emitter
    }

    fun count() = openCount.sum().toInt()

}