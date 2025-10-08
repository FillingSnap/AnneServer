package com.anne.server.presentation.api.story.controller

import com.anne.server.application.story.port.`in`.StoryUseCase
import com.anne.server.application.user.dto.AuthPrincipal
import com.anne.server.domain.Story
import com.anne.server.presentation.api.story.dto.GenerateRequest
import com.anne.server.presentation.validation.ValidationSequence
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/story")
class StoryController(

    private val storyUseCase: StoryUseCase

) {

    @Operation(summary = "스토리 단일 조회")
    @GetMapping("/{storyId}")
    fun getStoryById(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @PathVariable storyId: Long
    ): Story =
        storyUseCase.getStory(principal.id, storyId)

    @Operation(summary = "스토리 생성")
    @PostMapping(
        value = ["/generate"],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    ) fun generateDiary(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @RequestPart(value = "imageList") imageList: List<MultipartFile>?,
        @RequestPart(value = "requestList") @Validated(value = [ValidationSequence::class]) request: GenerateRequest
    ): List<Story> =
        storyUseCase.postStoryList(
            principal.id,
            request.uuid!!,
            imageList,
            request.textList
        )

}