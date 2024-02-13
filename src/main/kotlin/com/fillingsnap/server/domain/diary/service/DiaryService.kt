package com.fillingsnap.server.domain.diary.service

import com.fillingsnap.server.domain.diary.dao.DiaryRepository
import com.fillingsnap.server.domain.diary.dto.DiaryWithStudyDto
import com.fillingsnap.server.domain.diary.dto.SimpleDiaryDto
import com.fillingsnap.server.domain.user.domain.User
import com.fillingsnap.server.global.exception.CustomException
import com.fillingsnap.server.global.exception.ErrorCode
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class DiaryService (

    private val diaryRepository: DiaryRepository

) {

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