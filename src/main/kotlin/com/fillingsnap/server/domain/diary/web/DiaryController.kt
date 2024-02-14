package com.fillingsnap.server.domain.diary.web

import com.fillingsnap.server.domain.diary.dto.DiaryWithStudyDto
import com.fillingsnap.server.domain.diary.dto.SimpleDiaryDto
import com.fillingsnap.server.domain.diary.service.DiaryService
import io.swagger.v3.oas.annotations.Operation
import org.apache.coyote.Response
import org.springframework.http.ResponseEntity
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/diary")
class DiaryController (

    private val sendingOperations: SimpMessageSendingOperations,

    private val diaryService: DiaryService

) {

    @Operation(summary = "웹소켓 테스트(/queue/channel/{id}로 hello 전송, 삭제 예정)")
    @GetMapping("/test")
    fun test(id: Long) {
        sendingOperations.convertAndSend("/queue/channel/$id", "hello")
    }

    @Operation(summary = "일기 전체 조회")
    @GetMapping
    fun getDiaryList(): ResponseEntity<List<SimpleDiaryDto>> {
        return ResponseEntity.ok().body(diaryService.getDiaryList())
    }

    @Operation(summary = "일기 단일 조회")
    @GetMapping("/{id}")
    fun getDiaryList(@PathVariable("id") id: Long): ResponseEntity<DiaryWithStudyDto> {
        return  ResponseEntity.ok().body(diaryService.getDiaryById(id))
    }

}