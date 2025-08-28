package com.anne.server.global.ops

import com.anne.server.global.config.DrainFlag
import com.anne.server.global.registry.SseRegistry
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/internal")
class InternalController (

    private val drainFlag: DrainFlag,

    private val sseRegistry: SseRegistry

) {

    @Operation(summary = "Connection Draining Start")
    @PostMapping("/drain/start")
    fun startDrain(): Map<String, Boolean> {
        drainFlag.acceptNew.set(false)
        return mapOf("acceptNew" to false)
    }

    @Operation(summary = "Connection Draining Stop (에러 발생 시 롤백 용도)")
    @PostMapping("/drain/stop")
    fun stopDrain(): Map<String, Boolean> {
        drainFlag.acceptNew.set(true)
        return mapOf("acceptNew" to true)
    }

    @Operation(summary = "SSE 연결 개수 확인")
    @GetMapping("/drain/status")
    fun status() = mapOf(
        "open" to sseRegistry.count(),
        "acceptNew" to drainFlag.acceptNew.get()
    )

}