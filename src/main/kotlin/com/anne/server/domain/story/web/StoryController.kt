package com.anne.server.domain.story.web

import com.anne.server.domain.story.dto.request.StoryGenerateRequestDto
import com.anne.server.domain.story.dto.response.StorySimpleResponseDto
import com.anne.server.domain.story.service.StoryService
import com.anne.server.global.validation.ValidationSequence
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/story")
class StoryController (

    private val storyService: StoryService

) {

    @Operation(summary = "스토리 단일 조회")
    @GetMapping("/{id}")
    fun getStoryById(@PathVariable id: Long): ResponseEntity<StorySimpleResponseDto> {
        return ResponseEntity.ok().body(storyService.getStoryById(id))
    }

    @Operation(summary = "스토리 생성")
    @PostMapping(
        value = ["/generate"],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    ) fun generateDiary(
        @RequestPart(value = "imageList") imageList: List<MultipartFile>?,
        @RequestPart(value = "requestList") @Validated(value = [ValidationSequence::class]) request: StoryGenerateRequestDto
    ): ResponseEntity<List<StorySimpleResponseDto>> {
        return ResponseEntity.ok().body(storyService.createStories(imageList, request))
    }

}