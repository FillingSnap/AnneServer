package com.fillingsnap.server.domain.story.service

import com.fillingsnap.server.domain.diary.dao.DiaryRepository
import com.fillingsnap.server.domain.story.dao.StoryRepository
import com.fillingsnap.server.domain.story.domain.Story
import com.fillingsnap.server.domain.story.dto.SimpleStudyDto
import com.fillingsnap.server.domain.user.domain.User
import com.fillingsnap.server.global.exception.CustomException
import com.fillingsnap.server.global.exception.ErrorCode
import com.fillingsnap.server.infra.oracle.ObjectStorageService
import com.fillingsnap.server.infra.redis.RedisDao
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime


@Service
class StoryService(

    private val storyRepository: StoryRepository,

    private val objectStorageService: ObjectStorageService,

    private val redisDao: RedisDao

) {

    fun getStoryById(id: Long): SimpleStudyDto {
        val study = storyRepository.findByIdOrNull(id)
            ?: throw CustomException(ErrorCode.STORY_NOT_FOUND)

        val user = SecurityContextHolder.getContext().authentication.principal as User

        // 해당 스토리의 소유자가 아닌 경우
        if (user.id!! != study.user.id!!) {
            throw CustomException(ErrorCode.NOT_YOUR_STORY)
        }

        return SimpleStudyDto(study)
    }

    fun createStories(imageList: List<MultipartFile>, textList: List<String>, uuid: String): List<SimpleStudyDto> {
        if (imageList.size != textList.size) {
            throw CustomException(ErrorCode.IMAGE_TEXT_NOT_MATCHING)
        }

        val user = SecurityContextHolder.getContext().authentication.principal as User

        val storyList = arrayListOf<Story>()
        val savedImageList =  arrayListOf<String>()

        for (i in imageList.indices) {
            val image: String = try {
                objectStorageService.uploadFile(imageList[i])
            } catch (e: Exception) {
                for (image in savedImageList) {
                    objectStorageService.deleteFile(image)
                }
                throw CustomException(ErrorCode.IMAGE_SAVE_ERROR)
            }
            savedImageList.add(image)
            val story = Story(
                text = textList[i],
                image = image,
                user = user,
                uuid = uuid
            )
            storyList.add(story)
        }

        return storyRepository.saveAll(storyList).map { SimpleStudyDto(it) }
    }

    @Scheduled(cron = "0 0 * * * *")
    fun deleteStories() {
        val nullDiaryStoryList = storyRepository.findAllByDiary(null)
        val deleteStoryList = arrayListOf<Story>()
        for (story in nullDiaryStoryList) {
            if (redisDao.getValues(story.uuid) == null) {
                try {
                    objectStorageService.deleteFile(story.image)
                    deleteStoryList.add(story)
                } catch (_: Exception) {
                    ;
                }
            }
        }

        storyRepository.deleteAll(deleteStoryList)
    }

}