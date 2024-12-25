package com.anne.server.domain.story.service

import com.anne.server.domain.diary.dao.DiaryRepository
import com.anne.server.domain.diary.dto.DiaryDto
import com.anne.server.domain.story.dto.request.GenerateRequest
import com.anne.server.domain.story.dao.StoryRepository
import com.anne.server.domain.story.domain.Story
import com.anne.server.domain.story.dto.response.StoryResponse
import com.anne.server.domain.user.domain.User
import com.anne.server.domain.user.dto.UserDto
import com.anne.server.global.exception.CustomException
import com.anne.server.global.exception.ErrorCode
import com.anne.server.infra.amazon.service.S3Service
import com.anne.server.logger
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO


@Service
class StoryService(

    private val storyRepository: StoryRepository,

    private val diaryRepository: DiaryRepository,

    private val s3Service: S3Service

) {

    private val log = logger()

    @Transactional(readOnly = true)
    fun getStoryById(id: Long): StoryResponse {
        val study = storyRepository.findByIdOrNull(id)
            ?: throw CustomException(ErrorCode.STORY_NOT_FOUND)

        val userDto = SecurityContextHolder.getContext().authentication.principal as UserDto

        // 해당 스토리의 소유자가 아닌 경우
        if (userDto.id != study.user.id!!) {
            throw CustomException(ErrorCode.NOT_YOUR_STORY)
        }

        return StoryResponse(study)
    }

    @Transactional
    fun createStories(imageList: List<MultipartFile>?, request: GenerateRequest): List<StoryResponse> {
        val textList = request.textList
        val uuid = request.uuid!!

        if (diaryRepository.existsDiaryByUuid(uuid)) {
            throw CustomException(ErrorCode.ALREADY_EXIST_UUID)
        }

        if (imageList == null || textList == null) {
            throw CustomException(ErrorCode.IMAGE_TEXT_REQUIRED)
        } else if (imageList.size != textList.size) {
            throw CustomException(ErrorCode.IMAGE_TEXT_NOT_MATCHING)
        }

        val userDto = SecurityContextHolder.getContext().authentication.principal as UserDto

        val storyList = arrayListOf<Story>()
        val savedImageList =  arrayListOf<String>()

        for (i in imageList.indices) {
            val image: String = try {
                s3Service.uploadObject(imageList[i])
            } catch (e: Exception) {
                for (image in savedImageList) {
                    s3Service.deleteObject(image)
                }
                throw CustomException(ErrorCode.IMAGE_SAVE_ERROR)
            }
            savedImageList.add(image)
            val story = Story(
                text = textList[i],
                image = image,
                user = UserDto.toEntity(userDto),
                uuid = uuid
            )
            storyList.add(story)
        }

        return storyRepository.saveAll(storyList).map { StoryResponse(it) }
    }

    @Transactional
    fun updateDiaryByUuid(diaryDto: DiaryDto) {
        val diary = DiaryDto.toEntity(diaryDto)
        val storyList = storyRepository.findAllByUuid(diaryDto.uuid)
        storyList.forEach { it.diary = diary }
        storyRepository.saveAll(storyList)
        log.info("Story List Updated: {}", diaryDto.uuid)
    }

    @Transactional(readOnly = true)
    fun getResizedImageAndTextByUuid(uuid: String): List<Pair<String, String>> {
        val storyList = storyRepository.findAllByUuid(uuid)

        return storyList.map {
            Pair(Base64.getEncoder().encodeToString(imageResize(it.image)), it.text)
        }
    }

    private fun imageResize(image: String): ByteArray {
        val inputStream = s3Service.getObject(image)

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

}