package com.anne.server.domain.diary.web

import com.anne.server.domain.diary.dto.request.DiaryCreateRequestDto
import com.anne.server.domain.diary.dto.response.DiaryWithStoryResponseDto
import com.anne.server.domain.diary.dto.request.DiaryGenerateRequestDto
import com.anne.server.domain.diary.service.DiaryService
import com.anne.server.global.validation.ValidationSequence
import com.anne.server.global.websocket.dto.WebSocketResponseDto
import com.anne.server.infra.openai.OpenAiService
import com.anne.server.infra.openai.dto.SseResponseDto
import io.swagger.v3.oas.annotations.Operation
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/diary")
class DiaryController (

    private val diaryService: DiaryService,

    private val openAiService: OpenAiService

) {

    @Operation(summary = "일기 생성")
    @PostMapping("/generate", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun generateDiary(
        @RequestPart(value = "imageList") imageList: List<MultipartFile>?,
        @RequestPart(value = "requestList") @Validated(value = [ValidationSequence::class]) request: DiaryGenerateRequestDto
    ): ResponseEntity<Unit> {
        return ResponseEntity.ok().body(diaryService.generateDiary(imageList, request))
    }

    @Operation(summary = "임시 일기 조회")
    @GetMapping("/temporal/{uuid}")
    fun getTemporalDiary(@PathVariable("uuid") uuid: String): ResponseEntity<String> {
        return ResponseEntity.ok().body(diaryService.getTemporalDiary(uuid))
    }

    @Operation(summary = "일기 저장")
    @PostMapping("/create")
    fun createDiary(@RequestBody @Validated(value = [ValidationSequence::class]) request: DiaryCreateRequestDto): ResponseEntity<DiaryWithStoryResponseDto> {
        return ResponseEntity.ok().body(diaryService.createDiary(request))
    }

    @Operation(summary = "일기 전체 조회")
    @GetMapping
    fun getDiaryList(
        @PageableDefault(size = 15, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<Page<DiaryWithStoryResponseDto>> {
        return ResponseEntity.ok().body(diaryService.getDiaryList(pageable))
    }

    @Operation(summary = "일기 단일 조회")
    @GetMapping("/{id}")
    fun getDiaryList(@PathVariable("id") id: Long): ResponseEntity<DiaryWithStoryResponseDto> {
        return ResponseEntity.ok().body(diaryService.getDiaryById(id))
    }

    @Operation(summary = "SSE 테스트")
    @GetMapping("/test", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun test(): Flux<SseResponseDto> {
        return openAiService.test("eab0dc71-1b97-4228-ab51-5f92c1ac9315", 1)
    }

}