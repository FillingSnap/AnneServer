package com.anne.server.domain.diary.web

import com.anne.server.domain.diary.dto.request.DiaryCreateRequestDto
import com.anne.server.domain.diary.dto.response.DiaryWithStoryResponseDto
import com.anne.server.domain.diary.dto.request.DiaryGenerateRequestDto
import com.anne.server.domain.diary.service.DiaryService
import com.anne.server.global.validation.ValidationSequence
import com.anne.server.global.websocket.dto.WebSocketResponseDto
import com.anne.server.global.websocket.WebSocketStatus
import io.swagger.v3.oas.annotations.Operation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.concurrent.TimeUnit

@RestController
@RequestMapping("/diary")
class DiaryController (

    private val sendingOperations: SimpMessageSendingOperations,

    private val diaryService: DiaryService

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
                    status = WebSocketStatus.SUCCESS,
                    content = string
                )

                sendingOperations.convertAndSend("/queue/channel/$id", response)
                TimeUnit.SECONDS.sleep(1)
            }
            sendingOperations.convertAndSend(
                "/queue/channel/$id",
                WebSocketResponseDto(
                    WebSocketStatus.EOF,
                    null
                )
            )
        }
    }

    @Operation(summary = "일기 전체 조회")
    @GetMapping
    fun getDiaryList(): ResponseEntity<List<DiaryWithStoryResponseDto>> {
        return ResponseEntity.ok().body(diaryService.getDiaryList())
    }

    @Operation(summary = "일기 전체 조회(Pageable)")
    @GetMapping("/pageable")
    fun getDiaryListPageable(
        @PageableDefault(size = 10, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<Page<DiaryWithStoryResponseDto>> {
        return ResponseEntity.ok().body(diaryService.getDiaryListPageable(pageable))
    }

    @Operation(summary = "일기 단일 조회")
    @GetMapping("/{id}")
    fun getDiaryList(@PathVariable("id") id: Long): ResponseEntity<DiaryWithStoryResponseDto> {
        return  ResponseEntity.ok().body(diaryService.getDiaryById(id))
    }

}