package com.fillingsnap.server.domain.story.web

import com.fillingsnap.server.domain.story.dto.SimpleStudyDto
import com.fillingsnap.server.domain.story.dto.request.StoryCreateRequestDto
import com.fillingsnap.server.domain.story.service.StoryService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

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

    @Operation(summary = "오늘 생성된 스토리 조회(일기 생성 전)")
    @GetMapping("/list/today")
    fun getTodayStoryList(): ResponseEntity<List<SimpleStudyDto>> {
        return ResponseEntity.ok().body(storyService.getTodayStoryList())
    }

    @Operation(summary = "스토리 생성")
    @PostMapping("/create", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createStory(
        @RequestPart(value = "image") image: MultipartFile,
        @RequestPart(value = "request") request: StoryCreateRequestDto
    ): ResponseEntity<SimpleStudyDto> {
        return ResponseEntity.ok().body(storyService.createStory(image, request))
    }

}