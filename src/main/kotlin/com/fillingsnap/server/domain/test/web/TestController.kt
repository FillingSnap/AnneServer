package com.fillingsnap.server.domain.test.web

import com.fillingsnap.server.domain.test.service.TestService
import com.fillingsnap.server.domain.test.vo.AnalysisResultVo
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/test")
class TestController(

    private val testService: TestService

) {

    @PostMapping("/analysis")
    fun sentimentAnalysis(@RequestBody plainText: String): ResponseEntity<AnalysisResultVo> {
        return ResponseEntity.ok().body(testService.sentimentAnalysis(plainText))
    }

}