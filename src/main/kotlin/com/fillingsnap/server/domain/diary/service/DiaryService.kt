package com.fillingsnap.server.domain.diary.service

import com.fillingsnap.server.domain.diary.dao.DiaryRepository
import com.fillingsnap.server.domain.diary.dto.DiaryWithStudyDto
import com.fillingsnap.server.domain.diary.dto.SimpleDiaryDto
import com.fillingsnap.server.global.exception.CustomException
import com.fillingsnap.server.global.exception.ErrorCode
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class DiaryService (

    private val diaryRepository: DiaryRepository

) {

    fun getDiaryList(): List<SimpleDiaryDto> {
        return diaryRepository.findAll().map {
            SimpleDiaryDto(it)
        }
    }

    fun getDiaryById(id: Long): DiaryWithStudyDto {
        return DiaryWithStudyDto(
            diaryRepository.findByIdOrNull(id)
                ?: throw CustomException(ErrorCode.DIARY_NOT_FOUND)
        )
    }

}