package com.anne.server.domain.diary.service

import com.anne.server.domain.diary.dao.DiaryRepository
import com.anne.server.domain.diary.dto.response.DiaryWithStoryResponseDto
import com.anne.server.domain.diary.dto.request.DiaryGenerateRequestDto
import com.anne.server.domain.diary.dto.request.DiaryUpdateRequestDto
import com.anne.server.domain.story.service.StoryService
import com.anne.server.domain.user.domain.User
import com.anne.server.global.exception.CustomException
import com.anne.server.global.exception.ErrorCode
import com.anne.server.infra.amazon.AwsS3Service
import com.anne.server.infra.openai.OpenAiService
import com.anne.server.infra.openai.dto.SseResponseDto
import com.anne.server.infra.openai.dto.SseStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import reactor.core.publisher.Flux
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO

@Service
class DiaryService (

    private val diaryRepository: DiaryRepository,

    private val storyService: StoryService,

    private val awsS3Service: AwsS3Service,

    private val openAiService: OpenAiService,

) {

    fun imageResize(image: String): ByteArray {
        val inputStream = awsS3Service.getObject(image)

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

    fun generateDiary(imageList: List<MultipartFile>?, request: DiaryGenerateRequestDto, delay: Long): Flux<SseResponseDto> {
        val textList = request.textList
        val uuid = request.uuid!!

        if (diaryRepository.existsDiaryByUuid(uuid)) {
            return Flux.just(
                SseResponseDto(
                    status = SseStatus.ERROR,
                    content = ErrorCode.ALREADY_EXIST_UUID.message
                )
            )
        }

        val storyList = storyService.createStories(imageList, textList, uuid)

        val imageTextList = storyList.map {
            Pair(Base64.getEncoder().encodeToString(imageResize(it.image)), it.text)
        }

        return openAiService.openAi(uuid, imageTextList, delay)
    }

    fun getDiaryList(pageable: Pageable): Page<DiaryWithStoryResponseDto> {
        val user = SecurityContextHolder.getContext().authentication.principal as User

        val page = diaryRepository.findAllByUser(user, pageable).map {
            DiaryWithStoryResponseDto(it)
        }

        if (page.totalPages <= page.number) {
            throw CustomException(ErrorCode.WRONG_PAGE)
        }

        return page
    }

    fun getDiaryById(id: Long): DiaryWithStoryResponseDto {
        val diary = diaryRepository.findByIdOrNull(id)
            ?: throw CustomException(ErrorCode.DIARY_NOT_FOUND)

        val user = SecurityContextHolder.getContext().authentication.principal as User

        // 해당 일기의 소유자가 아닌 경우
        if (user.id!! != diary.user.id!!) {
            throw CustomException(ErrorCode.NOT_YOUR_DIARY)
        }

        return DiaryWithStoryResponseDto(diary)
    }

    fun updateDiary(id: Long, request: DiaryUpdateRequestDto): DiaryWithStoryResponseDto {
        val user = SecurityContextHolder.getContext().authentication.principal as User

        val diary = diaryRepository.findByIdOrNull(id)
            ?: throw CustomException(ErrorCode.DIARY_NOT_FOUND)

        // 해당 일기의 소유자가 아닌 경우
        if (user.id!! != diary.user.id!!) {
            throw CustomException(ErrorCode.NOT_YOUR_DIARY)
        }

        diary.content = request.content

        diaryRepository.save(diary)

        return DiaryWithStoryResponseDto(diary)
    }

}