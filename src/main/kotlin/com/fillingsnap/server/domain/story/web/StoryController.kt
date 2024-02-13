package com.fillingsnap.server.domain.story.web

import com.fillingsnap.server.domain.story.dto.SimpleStudyDto
import com.fillingsnap.server.domain.story.service.StoryService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/story")
class StoryController (

    private val storyService: StoryService

) {

    @Operation(summary = "스토리 단일 조회")
    @GetMapping("/{id}")
    fun getStoryById(@PathVariable id: Long): ResponseEntity<SimpleStudyDto> {
        return ResponseEntity.ok(storyService.getStoryById(id))
    }

    @Operation(summary = "오늘 생성된 스토리 조회(일기 생성 전)")
    @GetMapping("/list/today")
    fun getTodayStoryList(): ResponseEntity<List<SimpleStudyDto>> {
        return ResponseEntity.ok(storyService.getTodayStoryList())
    }

}