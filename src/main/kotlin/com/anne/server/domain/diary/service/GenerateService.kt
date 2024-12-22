package com.anne.server.domain.diary.service

import com.anne.server.domain.diary.dao.DiaryRepository
import com.anne.server.domain.diary.domain.Diary
import com.anne.server.domain.diary.dto.DiaryDto
import com.anne.server.domain.diary.dto.response.SseResponse
import com.anne.server.domain.diary.enums.SseStatus
import com.anne.server.domain.story.dao.StoryRepository
import com.anne.server.domain.story.service.StoryService
import com.anne.server.domain.user.dto.UserDto
import com.anne.server.global.exception.ErrorCode
import com.anne.server.infra.openai.service.OpenAiService
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import java.time.Duration

@Service
class GenerateService (

    private val diaryRepository: DiaryRepository,

    private val storyRepository: StoryRepository,

    private val storyService: StoryService,

    private val openAiService: OpenAiService

) {

    @Transactional
    fun generateDiary(delay: Long, uuid: String): Flux<SseResponse> {
        if (diaryRepository.existsDiaryByUuid(uuid)) {
            return Flux.just(
                SseResponse(
                    status = SseStatus.ERROR,
                    content = ErrorCode.ALREADY_EXIST_UUID.message
                )
            )
        }

        if (!storyRepository.existsStoryByUuid(uuid)) {
            return Flux.just(
                SseResponse(
                    status = SseStatus.ERROR,
                    content = ErrorCode.STORY_NOT_FOUND.message
                )
            )
        }

        val userDto = SecurityContextHolder.getContext().authentication.principal as UserDto
        val imageTextList = storyService.getResizedImageAndTextByUuid(uuid)

        var result = ""

        return openAiService.generateDiary(imageTextList, delay).map {
            if (it == "[DONE]") {
                SseResponse(
                    status = SseStatus.EOF,
                    content = result
                )
            } else {
                println(it)
                try {
                    val json = JSONParser().parse(it) as JSONObject
                    val choices = json["choices"] as JSONArray
                    val content = if (choices[0] != null) {
                        ((choices[0] as JSONObject)["delta"] as JSONObject)["content"] as String? ?: ""
                    } else {
                        ""
                    }
                    result += content
                    SseResponse(
                        status = SseStatus.SUCCESS,
                        content = content
                    )
                } catch (e: Exception) {
                    SseResponse(
                        status = SseStatus.ERROR,
                        content = it
                    )
                }
            }}
            .publishOn(Schedulers.boundedElastic())
            .doOnComplete {
                val newDiary = diaryRepository.save(
                    Diary(
                        emotion = "null",
                        content = result,
                        user = UserDto.toEntity(userDto),
                        uuid = uuid
                    )
                )

                storyService.updateDiaryByUuid(DiaryDto.fromEntity(newDiary))
            }
            .doOnError {
                SseResponse(
                    status = SseStatus.ERROR,
                    content = it.message
                )
            }
    }

    fun test(delay: Long): Flux<SseResponse> {
        val result = "식사를 준비하며 스테이크와 야채를 손질하는 것은 항상 맛있는 시간이다. 스테이크를구워내는 소리와 야채가 향기로 " +
                "가득한 주방에서의 시간은 언제나 아름다운 순간이다. 나무 도마 위에서 재료들을 다듬고 음식을 만들어내는 과정은 맛있는 " +
                "요리를 만들어냄으로써 즐거움을 더해준다. 이런 소중한 시간을 보내며, 맛있는 음식을 먹을 때의 만족감은 무엇과도 바꿀 수 " +
                "없는 특별한 순간이다. 하늘을 바라보며 구름이 맑은 하늘을 가려주는 모습은 평화로움과 상쾌함을 느끼게 해준다. 구름 " +
                "사이로 비치는 햇살은 마치 세상의 모든 불평이 사라지고 마음이 맑아지는 듯한 기분을 안겨준다. 이런 풍경을 바라보며 " +
                "산뜻한 순간에는 감사함을 느끼며, 새로운 하루를 시작하고 싶어진다. 카페에서 손님들을 맞이하며 커피를 내리고 소품을 " +
                "정리하는 일상적인 모습은 편안함을 안겨준다. 카페 안에서 일어나는 일상의 소소한 순간들을 바라보며, 이곳에서 보내는 " +
                "시간은 언제나 특별하고 소중하다. 손님들과 함께하는 이 시간은 편안하고 소중한 것이다. 손님들과 이야기를 나누며 커피 한 " +
                "잔을 즐기는 것은 일상적인 활동이지만, 그 속에는 소중한 소품들과 이야기가 담겨있어 더욱 의미있는 시간이다. 카페 " +
                "안에서의 일상은 특별함을 발견할 수 있는 소중한 시간이다. 이런 소중한 순간들을 보면서, 일상 속에 숨겨진 아름다움을 " +
                "발견하고 감사함을 느낀다. "
        val list = result.split("")
        val eventStream = Flux.fromIterable(list + "[DONE]")
            .delayElements(Duration.ofMillis(delay))
            .map {
                if (it == "[DONE]") {
                    SseResponse(
                        status = SseStatus.EOF,
                        content = result
                    )
                } else {
                    SseResponse(
                        status = SseStatus.SUCCESS,
                        content = it ?: ""
                    )
                }
            }.doOnError {
                SseResponse(
                    status = SseStatus.ERROR,
                    content = it.message
                )
            }

        return eventStream
    }

}