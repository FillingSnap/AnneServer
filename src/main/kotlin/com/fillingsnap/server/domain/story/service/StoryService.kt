package com.fillingsnap.server.domain.story.service

import com.fillingsnap.server.domain.story.dao.StoryRepository
import com.fillingsnap.server.domain.story.dto.SimpleStudyDto
import com.fillingsnap.server.global.exception.CustomException
import com.fillingsnap.server.global.exception.ErrorCode
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Service
class StoryService (

    private val storyRepository: StoryRepository

) {

    fun getStoryById(id: Long): SimpleStudyDto {
        return SimpleStudyDto(
            storyRepository.findByIdOrNull(id)
                ?: throw CustomException(ErrorCode.STORY_NOT_FOUND)
        )
    }

    fun getTodayStoryList(): List<SimpleStudyDto> {
        // todo: 당일 자 일기가 생성되었을 경우 에러
        val startDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0, 0))
        val endDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59, 59))
        return storyRepository.findAllByCreatedAtBetween(startDateTime, endDateTime).map {
            SimpleStudyDto(it)
        }
    }

}