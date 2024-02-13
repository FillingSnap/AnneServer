package com.fillingsnap.server.domain.diary.web

import com.fillingsnap.server.domain.diary.dto.DiaryWithStudyDto
import com.fillingsnap.server.domain.diary.dto.SimpleDiaryDto
import com.fillingsnap.server.domain.diary.service.DiaryService
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

    @GetMapping("/test")
    fun test(id: Long) {
        sendingOperations.convertAndSend("/queue/channel/$id", "hello")
    }

    @GetMapping("/")
    fun getDiaryList(): List<SimpleDiaryDto> {
        return diaryService.getDiaryList()
    }

    @GetMapping("/{id}")
    fun getDiaryList(@PathVariable("id") id: Long): DiaryWithStudyDto {
        return diaryService.getDiaryById(id)
    }

}