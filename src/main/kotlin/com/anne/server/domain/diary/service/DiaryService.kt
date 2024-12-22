package com.anne.server.domain.diary.service

import com.anne.server.domain.diary.dao.DiaryRepository
import com.anne.server.domain.diary.dto.response.DiaryResponse
import com.anne.server.domain.diary.dto.request.UpdateRequest
import com.anne.server.domain.user.dto.UserDto
import com.anne.server.global.exception.CustomException
import com.anne.server.global.exception.ErrorCode
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DiaryService(

    private val diaryRepository: DiaryRepository

) {

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

        return DiaryResponse(diary)
    }

}