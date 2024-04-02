package com.fillingsnap.server.domain.diary.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fillingsnap.server.domain.diary.dao.DiaryRepository
import com.fillingsnap.server.domain.diary.domain.Diary
import com.fillingsnap.server.domain.diary.dto.request.DiaryCreateRequestDto
import com.fillingsnap.server.domain.diary.dto.DiaryWithStudyDto
import com.fillingsnap.server.domain.diary.dto.SimpleDiaryDto
import com.fillingsnap.server.domain.diary.dto.request.DiaryGenerateRequestDto
import com.fillingsnap.server.domain.story.dao.StoryRepository
import com.fillingsnap.server.domain.story.service.StoryService
import com.fillingsnap.server.domain.user.domain.User
import com.fillingsnap.server.global.websocket.dto.WebSocketResponseDto
import com.fillingsnap.server.global.websocket.WebSocketStatus
import com.fillingsnap.server.global.exception.CustomException
import com.fillingsnap.server.global.exception.ErrorCode
import com.fillingsnap.server.infra.openai.OpenAiService
import com.fillingsnap.server.infra.oracle.ObjectStorageService
import com.fillingsnap.server.infra.redis.RedisDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.data.repository.findByIdOrNull
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.time.Duration
import java.util.*
import javax.imageio.ImageIO

@Service
class DiaryService (

    private val diaryRepository: DiaryRepository,

    private val storyRepository: StoryRepository,

    private val storyService: StoryService,

    private val objectStorageService: ObjectStorageService,

    private val openAiService: OpenAiService,

    private val sendingOperations: SimpMessageSendingOperations,

    private val redisDao: RedisDao

) {

    fun imageResize(image: String): ByteArray {
        val inputStream = objectStorageService.getObject(image)

        val sourceImage = ImageIO.read(inputStream)
        val width = 640
        val height = 640
        val resizeImage = BufferedImage(width, height, sourceImage.type)
        val g = resizeImage.createGraphics()
        g.drawImage(sourceImage, 0, 0, width, height, null)
        g.dispose()

        val byteArrayOutputStream = ByteArrayOutputStream()
        ImageIO.write(resizeImage, "png", byteArrayOutputStream)

        return byteArrayOutputStream.toByteArray()
    }

    fun generateDiary(imageList: List<MultipartFile>, request: DiaryGenerateRequestDto) {
        val user = SecurityContextHolder.getContext().authentication.principal as User
        val textList = request.textList
        val uuid = request.uuid
        val storyList = storyService.createStories(imageList, textList, uuid)

        val imageTextList = storyList.map {
            Pair(Base64.getEncoder().encodeToString(imageResize(it.image)), it.text)
        }

        sendingOperations.convertAndSend(
            "/queue/channel/${user.id!!}",
            WebSocketResponseDto(
                status = WebSocketStatus.UUID,
                content = uuid
            )
        )

        val coroutineScope = CoroutineScope(Dispatchers.IO)
        coroutineScope.launch {
            val result = try {
                openAiService.openAi(user.id!!, imageTextList)
            } catch (e: Exception) {
                sendingOperations.convertAndSend(
                    "/queue/channel/${user.id!!}",
                    WebSocketResponseDto(
                        status = WebSocketStatus.ERROR,
                        content = e.message
                    )
                )
                return@launch
            }

            val mapper = ObjectMapper().registerKotlinModule()
            mapper.registerModules(JavaTimeModule())

            sendingOperations.convertAndSend(
                "/queue/channel/${user.id!!}",
                WebSocketResponseDto(
                    status = WebSocketStatus.EOF,
                    content = result
                )
            )

            redisDao.setValues(uuid, result, Duration.ofMillis(600000L))
        }
    }

    fun getTemporalDiary(uuid: String): String {
        val temporalDiary = redisDao.getValues(uuid) ?: throw CustomException(ErrorCode.TEMPORAL_DIARY_NOT_FOUND)

        return temporalDiary
    }

    fun createDiary(request: DiaryCreateRequestDto): SimpleDiaryDto {
        val user = SecurityContextHolder.getContext().authentication.principal as User
        val uuid = request.uuid

        redisDao.getValues(uuid) ?: throw CustomException(ErrorCode.TEMPORAL_DIARY_NOT_FOUND)

        val storyList = storyRepository.findAllByUuid(uuid)

        val newDiary = diaryRepository.save(Diary(
            emotion = "null",
            content = request.content,
            user = user,
            uuid = uuid
        ))

        storyList.forEach { it.diary = newDiary }
        storyRepository.saveAll(storyList)

        redisDao.deleteValues(uuid)

        return SimpleDiaryDto(newDiary)
    }

    fun getDiaryList(): List<SimpleDiaryDto> {
        val user = SecurityContextHolder.getContext().authentication.principal as User

        return diaryRepository.findAllByUser(user).map {
            SimpleDiaryDto(it)
        }
    }

    fun getDiaryById(id: Long): DiaryWithStudyDto {
        val diary = diaryRepository.findByIdOrNull(id)
            ?: throw CustomException(ErrorCode.DIARY_NOT_FOUND)

        val user = SecurityContextHolder.getContext().authentication.principal as User

        // 해당 일기의 소유자가 아닌 경우
        if (user.id!! != diary.user.id!!) {
            throw CustomException(ErrorCode.NOT_YOUR_DIARY)
        }

        return DiaryWithStudyDto(diary)
    }

}