package com.fillingsnap.server.domain.diary.web

import com.fillingsnap.server.domain.diary.dto.DiaryWithStudyDto
import com.fillingsnap.server.domain.diary.dto.SimpleDiaryDto
import com.fillingsnap.server.domain.diary.service.DiaryService
import com.fillingsnap.server.global.config.websocket.WebSocketResponseDto
import com.fillingsnap.server.global.config.websocket.WebSocketStatus
import io.swagger.v3.oas.annotations.Operation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.http.ResponseEntity
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.TimeUnit

@RestController
@RequestMapping("/diary")
class DiaryController (

    private val sendingOperations: SimpMessageSendingOperations,

    private val diaryService: DiaryService

) {

    @Operation(summary = "웹소켓 테스트(/queue/channel/{id}로 hello 전송, 삭제 예정)")
    @GetMapping("/test")
    fun test(id: Long) {
        val coroutineScope = CoroutineScope(Dispatchers.IO)
        coroutineScope.launch {
            val list = arrayOf(
                "오늘은 카페에 와서 조용",
                "하게 책을 읽으며 편안",
                "한 시간을 보냈다. 테이블과",
                " 의자가 편안해서 계속 앉아",
                " 있고 싶었고, 커피숍",
                " 카운터에서는 따뜻한 음료를",
                " 마시며 창가에 앉아 유리창",
                "을 바라보았다. 책장에는 다",
                "양한 책들이 가득했고,",
                " 이곳에서 시간을 보내는 것이 정말",
                " 행복했다.\\\\e"
            )

            for (string in list) {
                val response = WebSocketResponseDto(
                    status = WebSocketStatus.SUCCESS.value,
                    content = string
                )

                sendingOperations.convertAndSend("/queue/channel/$id", response)
                TimeUnit.SECONDS.sleep(1)
            }
            sendingOperations.convertAndSend("/queue/channel/$id", WebSocketResponseDto(WebSocketStatus.EOF.value, null))
        }
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