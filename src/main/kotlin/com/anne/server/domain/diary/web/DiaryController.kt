package com.anne.server.domain.diary.web

import com.anne.server.domain.diary.dto.response.DiaryResponse
import com.anne.server.domain.diary.dto.request.UpdateRequest
import com.anne.server.domain.diary.service.DiaryService
import com.anne.server.global.validation.ValidationSequence
import com.anne.server.domain.diary.dto.response.SseResponse
import com.anne.server.domain.diary.service.GenerateService
import io.swagger.v3.oas.annotations.Operation
import jakarta.servlet.http.HttpServletRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/diary")
class DiaryController (

    private val diaryService: DiaryService,

    private val generateService: GenerateService

) {

    @Operation(summary = "일기 생성")
    @PostMapping(
        value = ["/generate"],
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE]
    ) fun generateDiary(
        @RequestParam delay: Long,
        @RequestBody uuid: String,
        request: HttpServletRequest
    ): ResponseEntity<SseEmitter> {
        return ResponseEntity.ok().body(generateService.generateDiary(delay, uuid, request))
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

    @Operation(summary = "일기 삭제")
    @DeleteMapping("/delete/{uuid}")
    fun deleteDiary(
        @PathVariable("uuid") uuid: String
    ): ResponseEntity<Unit> {
        return ResponseEntity.ok().body(diaryService.deleteDiary(uuid))
    }

}