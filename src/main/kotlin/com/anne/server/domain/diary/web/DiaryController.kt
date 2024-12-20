package com.anne.server.domain.diary.web

import com.anne.server.domain.diary.dto.response.DiaryResponse
import com.anne.server.domain.diary.dto.request.UpdateRequest
import com.anne.server.domain.diary.service.DiaryService
import com.anne.server.global.validation.ValidationSequence
import com.anne.server.infra.openai.OpenAiService
import com.anne.server.infra.openai.dto.SseResponse
import io.swagger.v3.oas.annotations.Operation
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/diary")
class DiaryController (

    private val diaryService: DiaryService,

    private val openAiService: OpenAiService

) {

    @Operation(summary = "일기 생성")
    @PostMapping(
        value = ["/generate"],
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE]
    ) fun generateDiary(
        @RequestParam delay: Long,
        @RequestBody uuid: String
    ): ResponseEntity<Flux<SseResponse>> {
        return ResponseEntity.ok().body(diaryService.generateDiary(delay, uuid))
    }

    @Operation(summary = "일기 전체 조회")
    @GetMapping
    fun getDiaryList(
        @PageableDefault(size = 15, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<Page<DiaryResponse>> {
        return ResponseEntity.ok().body(diaryService.getDiaryList(pageable))
    }

    @Operation(summary = "일기 단일 조회")
    @GetMapping("/{uuid}")
    fun getDiaryList(@PathVariable("uuid") uuid: String): ResponseEntity<DiaryResponse> {
        return ResponseEntity.ok().body(diaryService.getDiaryByUuid(uuid))
    }

    @Operation(summary = "일기 수정")
    @PutMapping("/update/{uuid}")
    fun updateDiary(
        @PathVariable("uuid") uuid: String,
        @RequestBody @Validated(value = [ValidationSequence::class]) request: UpdateRequest
    ): ResponseEntity<DiaryResponse> {
        return ResponseEntity.ok().body(diaryService.updateDiary(uuid, request))
    }

    @Operation(summary = "SSE 테스트")
    @GetMapping("/test", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun test(@RequestParam delay: Long): Flux<SseResponse> {
        return openAiService.test(delay)
    }

}