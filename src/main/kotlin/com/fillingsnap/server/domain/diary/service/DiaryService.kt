package com.fillingsnap.server.domain.diary.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fillingsnap.server.domain.diary.dao.DiaryRepository
import com.fillingsnap.server.domain.diary.domain.Diary
import com.fillingsnap.server.domain.diary.dto.DiaryWithStudyDto
import com.fillingsnap.server.domain.diary.dto.SimpleDiaryDto
import com.fillingsnap.server.domain.story.dao.StoryRepository
import com.fillingsnap.server.domain.user.domain.User
import com.fillingsnap.server.global.config.websocket.WebSocketResponseDto
import com.fillingsnap.server.global.config.websocket.WebSocketStatus
import com.fillingsnap.server.global.exception.CustomException
import com.fillingsnap.server.global.exception.ErrorCode
import com.fillingsnap.server.infra.openai.OpenAiService
import com.fillingsnap.server.infra.oracle.ObjectStorageService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.data.repository.findByIdOrNull
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import javax.imageio.ImageIO

@Service
class DiaryService (

    private val diaryRepository: DiaryRepository,

    private val storyRepository: StoryRepository,

    private val objectStorageService: ObjectStorageService,

    private val openAiService: OpenAiService,

    private val sendingOperations: SimpMessageSendingOperations,

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

    fun createDiary() {
        val user = SecurityContextHolder.getContext().authentication.principal as User

        val startDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0, 0))
        val endDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59, 59))
        val diary = diaryRepository.findByUserAndCreatedAtBetween(user, startDateTime, endDateTime)

        if (diary != null) {
            throw CustomException(ErrorCode.TODAY_DIARY_ALREADY_EXIST)
        }

        val todayStoryList = storyRepository.findAllByCreatedAtBetween(startDateTime, endDateTime)

        val imageTextList = todayStoryList.map {
            Pair(Base64.getEncoder().encodeToString(imageResize(it.image)), it.text)
        }

        val coroutineScope = CoroutineScope(Dispatchers.IO)
        coroutineScope.launch {
            val result = openAiService.openAi(user.id!!, imageTextList)

            val newDiary = diaryRepository.save(Diary(
                emotion = "null",
                content = result,
                user = user
            ))

            val mapper = ObjectMapper().registerKotlinModule()
            mapper.registerModules(JavaTimeModule())

            sendingOperations.convertAndSend(
                "/queue/channel/${user.id!!}",
                WebSocketResponseDto(
                    status = WebSocketStatus.EOF.value,
                    content = mapper.writeValueAsString(SimpleDiaryDto(newDiary))
                )
            )
        }
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