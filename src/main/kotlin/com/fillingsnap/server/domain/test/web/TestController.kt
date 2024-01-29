package com.fillingsnap.server.domain.test.web

import com.aallam.openai.api.chat.ChatCompletion
import com.fillingsnap.server.domain.test.service.TestService
import com.fillingsnap.server.domain.test.vo.AnalysisResultVo
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/test")
class TestController(

    private val testService: TestService

) {

    @PostMapping("/analysis")
    fun sentimentAnalysis(@RequestBody plainText: String): ResponseEntity<AnalysisResultVo> {
        return ResponseEntity.ok().body(testService.sentimentAnalysis(plainText))
    }

    @GetMapping("/openai")
    suspend fun openai(): ResponseEntity<String> {
        return ResponseEntity.ok().body(testService.openAI())
    }

}