package com.anne.server.application.diary.port.`in`

import com.anne.server.domain.Diary
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface DiaryUseCase {

    fun getDiaryByUuid(userId: Long, uuid: String): Diary

    fun getDiaryList(userId: Long, pageable: Pageable): Page<Diary>

    fun putDiary(userId: Long, uuid: String, content: String): Diary

    fun deleteDiary(userId: Long, uuid: String)

}