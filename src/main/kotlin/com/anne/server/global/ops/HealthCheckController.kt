package com.anne.server.global.ops

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthCheckController {

    @GetMapping
    fun healthCheck(): Map<String, String> {
        return mapOf("status" to "UP")
    }

}