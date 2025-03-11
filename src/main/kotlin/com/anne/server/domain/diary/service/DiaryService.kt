package com.anne.server.domain.diary.service

import com.anne.server.domain.diary.dao.DiaryRepository
import com.anne.server.domain.diary.domain.Diary
import com.anne.server.domain.diary.dto.DiaryDto
import com.anne.server.domain.diary.dto.response.DiaryResponse
import com.anne.server.domain.diary.dto.request.UpdateRequest
import com.anne.server.domain.story.service.StoryService
import com.anne.server.domain.user.dto.UserDto
import com.anne.server.global.exception.exceptions.CustomException
import com.anne.server.global.exception.enums.ErrorCode
import com.anne.server.logger
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DiaryService(

    private val diaryRepository: DiaryRepository,

    private val storyService: StoryService

) {

    private val log = logger()

    @Transactional(readOnly = true)
    fun getDiaryList(pageable: Pageable): Page<DiaryResponse> {
        val userDto = SecurityContextHolder.getContext().authentication.principal as UserDto

        val page = diaryRepository.findAllByUser(UserDto.toEntity(userDto), pageable).map {
            DiaryResponse(it)
        }

        if (page.totalPages != 0 && page.totalPages <= page.number) {
            throw CustomException(ErrorCode.WRONG_PAGE)
        }

        return page
    }

    @Transactional(readOnly = true)
    fun getDiaryByUuid(uuid: String): DiaryResponse {
        val diary = diaryRepository.findByUuid(uuid)
            ?: throw CustomException(ErrorCode.DIARY_NOT_FOUND)

        val userDto = SecurityContextHolder.getContext().authentication.principal as UserDto
        val user = UserDto.toEntity(userDto)

        // 해당 일기의 소유자가 아닌 경우
        if (user.id!! != diary.user.id!!) {
            throw CustomException(ErrorCode.NOT_YOUR_DIARY)
        }

        return DiaryResponse(diary)
    }

    @Transactional
    fun saveDiary(userDto: UserDto, content: String, uuid: String) {
        val newDiary = diaryRepository.save(
            Diary(
                emotion = "null",
                content = content,
                user = UserDto.toEntity(userDto),
                uuid = uuid
            )
        )
        log.info("Diary Saved: {}", uuid)

        storyService.updateDiaryByUuid(DiaryDto.fromEntity(newDiary))
    }

    @Transactional
    fun updateDiary(uuid: String, request: UpdateRequest): DiaryResponse {
        val userDto = SecurityContextHolder.getContext().authentication.principal as UserDto

        val diary = diaryRepository.findByUuid(uuid)
            ?: throw CustomException(ErrorCode.DIARY_NOT_FOUND)

        // 해당 일기의 소유자가 아닌 경우
        if (userDto.id != diary.user.id!!) {
            throw CustomException(ErrorCode.NOT_YOUR_DIARY)
        }

        diary.content = request.content

        diaryRepository.save(diary)
        log.info("Diary Updated: {}", uuid)

        return DiaryResponse(diary)
    }

    @Transactional
    fun deleteDiary(uuid: String) {
        val userDto = SecurityContextHolder.getContext().authentication.principal as UserDto

        val diary = diaryRepository.findByUuid(uuid)
            ?: throw CustomException(ErrorCode.DIARY_NOT_FOUND)

        // 해당 일기의 소유자가 아닌 경우
        if (userDto.id != diary.user.id!!) {
            throw CustomException(ErrorCode.NOT_YOUR_DIARY)
        }

        diaryRepository.delete(diary)
        log.info("Diary Deleted: {}", uuid)
    }

}