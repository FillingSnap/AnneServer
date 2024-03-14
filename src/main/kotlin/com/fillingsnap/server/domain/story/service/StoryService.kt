package com.fillingsnap.server.domain.story.service

import com.fillingsnap.server.domain.diary.dao.DiaryRepository
import com.fillingsnap.server.domain.story.dao.StoryRepository
import com.fillingsnap.server.domain.story.domain.Story
import com.fillingsnap.server.domain.story.dto.SimpleStudyDto
import com.fillingsnap.server.domain.story.dto.StoryCreateRequestDto
import com.fillingsnap.server.domain.user.domain.User
import com.fillingsnap.server.global.exception.CustomException
import com.fillingsnap.server.global.exception.ErrorCode
import com.fillingsnap.server.infra.oracle.ObjectStorageService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime


@Service
class StoryService(

    private val storyRepository: StoryRepository,

    private val diaryRepository: DiaryRepository,

    private val objectStorageService: ObjectStorageService

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

    fun getTodayStoryList(): List<SimpleStudyDto> {
        val user = SecurityContextHolder.getContext().authentication.principal as User

        val startDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0, 0))
        val endDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59, 59))
        val diary = diaryRepository.findByUserAndCreatedAtBetween(user, startDateTime, endDateTime)

        if (diary != null) {
            throw CustomException(ErrorCode.TODAY_DIARY_ALREADY_EXIST)
        }

        return storyRepository.findAllByCreatedAtBetween(startDateTime, endDateTime).map {
            SimpleStudyDto(it)
        }
    }

    fun createStory(file: MultipartFile, request: StoryCreateRequestDto): SimpleStudyDto {
        val user = SecurityContextHolder.getContext().authentication.principal as User
        val image = objectStorageService.uploadFile(file)
        val story = Story(
            text = request.text,
            image = image,
            user = user,
        )

        return SimpleStudyDto(storyRepository.save(story))
    }

    fun uploadFile(file: MultipartFile): String {
        return objectStorageService.uploadFile(file)
    }

}