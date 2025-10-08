package com.anne.server.application.diary.port.out

import com.anne.server.domain.Diary
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface DiaryRepository {

    fun existsByUuid(uuid: String): Boolean

    fun findByUuid(uuid: String): Diary?

    fun findAllByUserId(userId: Long, pageable: Pageable): Page<Diary>

    fun save(diary: Diary): Diary

    fun delete(diary: Diary)

}