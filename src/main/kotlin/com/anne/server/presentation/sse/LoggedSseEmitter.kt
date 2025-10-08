package com.anne.server.presentation.sse

import com.anne.server.common.alert.SseErrorInfo
import com.anne.server.common.log.logger
import com.anne.server.infrastructure.alert.adapter.JdaAdapter
import org.slf4j.MDC
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class LoggedSseEmitter(

    private val jdaAdapter: JdaAdapter

): SseEmitter(5 * 60 * 1000) {

    private val log = logger()

    private val startTime: Long = System.currentTimeMillis()

    override fun send(obj: Any) {
        runCatching { super.send(obj) }
            .onSuccess { log.debug(obj.toString()) }
            .onFailure { this.completeWithError(it) }
    }

    fun successWith(obj: Any) {
        runCatching { super.send(obj) }
            .onSuccess {
                val elapsed = (System.currentTimeMillis() - startTime) / 1000.0
                log.info("""
                    |
                    |[SSE RESPONSE] ${"%.3f".format(elapsed)}s
                    |>> RESULT: ${obj})
                """.trimMargin())
            }
            .onFailure { this.completeWithError(it) }
    }

    // 서버에서 예측 가능한 에러
    fun failedWith(obj: Any, t: Throwable) {
        runCatching { super.send(obj) }
            .onSuccess {
                val elapsed = (System.currentTimeMillis() - startTime) / 1000.0
                log.error("""
                    |
                    |[SSE RESPONSE] ${"%.3f".format(elapsed)}s
                    |>> RESULT: ${obj})
                    |>> ERROR: ${t.message}
                """.trimMargin())

                jdaAdapter.alertError(
                    SseErrorInfo(
                        requestId = MDC.get("requestId"),
                        elapsedTime = elapsed,
                        result = obj.toString(),
                        error = t.message
                    )
                )
            }
            .onFailure { this.completeWithError(it) }
    }

    // 서버에서 예측 불가능한 에러
    override fun completeWithError(ex: Throwable) {
        val elapsed = (System.currentTimeMillis() - startTime) / 1000.0
        super.completeWithError(ex)
        log.error("""
            |
            |[SSE RESPONSE] ${"%.3f".format(elapsed)}s
            |>> RESULT: ${ex.message})
        """.trimMargin())
    }

}