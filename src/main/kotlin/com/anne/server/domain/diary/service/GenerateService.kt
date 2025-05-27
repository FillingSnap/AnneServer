package com.anne.server.domain.diary.service

import com.anne.server.domain.diary.dao.DiaryRepository
import com.anne.server.domain.story.dao.StoryRepository
import com.anne.server.domain.story.service.StoryService
import com.anne.server.domain.user.dto.UserDto
import com.anne.server.global.exception.exceptions.CustomException
import com.anne.server.global.exception.enums.ErrorCode
import com.anne.server.global.logging.wrapper.SseEmitterLoggingWrapper
import com.anne.server.infra.ai.dao.AiService
import com.anne.server.infra.discord.BotService
import com.anne.server.infra.openai.service.OpenAiService
import jakarta.servlet.http.HttpServletRequest
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import reactor.core.scheduler.Schedulers

@Service
class GenerateService (

    private val diaryRepository: DiaryRepository,

    private val storyRepository: StoryRepository,

    private val diaryService: DiaryService,

    private val storyService: StoryService,

    // private val openAiService: OpenAiService,

    private val botService: BotService,

    private val aiService: AiService

) {

    @Transactional
    fun generateDiary(delay: Long, uuid: String, request: HttpServletRequest): SseEmitter {
        val emitter = SseEmitterLoggingWrapper(botService, request)

        if (diaryRepository.existsDiaryByUuid(uuid)) {
            emitter.completeWithError(CustomException(ErrorCode.ALREADY_EXIST_UUID))
            return emitter
        }

        if (!storyRepository.existsStoryByUuid(uuid)) {
            emitter.completeWithError(CustomException(ErrorCode.STORY_NOT_FOUND))
            return emitter
        }

        val userDto = SecurityContextHolder.getContext().authentication.principal as UserDto
        val imageTextList = storyService.getImageAndTextByUuid(uuid)
        var result = ""

        aiService.generateDiary(imageTextList, delay)
            .doOnNext { response ->
                result += response

                try {
                    emitter.send(response)
                } catch (e : Exception) {}
            }
            .doOnError(emitter::completeWithError)
            .publishOn(Schedulers.boundedElastic())
            .doOnComplete {
                diaryService.saveDiary(userDto, result, uuid)
                try {
                    emitter.complete(result)
                } catch (e : Exception) {
                    emitter.completeWithError(e)
                    return@doOnComplete
                }
            }
            .subscribe()

        return emitter
    }

    /*
    @Transactional
    fun generateDiary(delay: Long, uuid: String, request: HttpServletRequest): SseEmitter {
        val emitter = SseEmitterLoggingWrapper(botService, request)

        if (diaryRepository.existsDiaryByUuid(uuid)) {
            emitter.completeWithError(CustomException(ErrorCode.ALREADY_EXIST_UUID))
            return emitter
        }

        if (!storyRepository.existsStoryByUuid(uuid)) {
            emitter.completeWithError(CustomException(ErrorCode.STORY_NOT_FOUND))
            return emitter
        }

        val userDto = SecurityContextHolder.getContext().authentication.principal as UserDto
        val imageTextList = storyService.getResizedImageAndTextByUuid(uuid)
        var result = ""

        openAiService.generateDiary(imageTextList, delay)
            .doOnNext { response ->
                if (response.equals("[DONE]")) {
                    return@doOnNext
                }
                val json = JSONParser().parse(response) as JSONObject
                val choices = json["choices"] as JSONArray
                val content = if (choices[0] != null) {
                    ((choices[0] as JSONObject)["delta"] as JSONObject)["content"] as String? ?: ""
                } else {
                    ""
                }
                result += content

                try {
                    emitter.send(content)
                } catch (e : Exception) {}
            }
            .doOnError(emitter::completeWithError)
            .publishOn(Schedulers.boundedElastic())
            .doOnComplete {
                diaryService.saveDiary(userDto, result, uuid)
                try {
                    emitter.complete(result)
                } catch (e : Exception) {
                    emitter.completeWithError(e)
                    return@doOnComplete
                }
            }
            .subscribe()

        return emitter
    }
     */

}