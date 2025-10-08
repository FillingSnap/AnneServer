package com.anne.server.presentation.api.health_check

import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/healthCheck")
class HealthCheckController {

    @Operation(summary = "Health check")
    @GetMapping("")
    fun healthCheck(): Map<String, String> {
        return mapOf("status" to "UP")
    }

}