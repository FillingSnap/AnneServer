package com.anne.server.domain.story.web

import com.anne.server.domain.story.dto.SimpleStudyDto
import com.anne.server.domain.story.service.StoryService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/story")
class StoryController (

    private val storyService: StoryService

) {

    @Operation(summary = "스토리 단일 조회")
    @GetMapping("/{id}")
    fun getStoryById(@PathVariable id: Long): ResponseEntity<SimpleStudyDto> {
        return ResponseEntity.ok().body(storyService.getStoryById(id))
    }

}