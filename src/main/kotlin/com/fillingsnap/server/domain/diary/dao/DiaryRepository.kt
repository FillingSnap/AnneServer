package com.fillingsnap.server.domain.diary.dao

import com.fillingsnap.server.domain.diary.domain.Diary
import com.fillingsnap.server.domain.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface DiaryRepository: JpaRepository<Diary, Long> {

    fun findAllByUser(user: User): List<Diary>

    fun existsDiaryByUuid(uuid: String): Boolean

}