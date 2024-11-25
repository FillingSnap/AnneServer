package com.anne.server.domain.diary.dao

import com.anne.server.domain.diary.domain.Diary
import com.anne.server.domain.user.domain.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface DiaryRepository: JpaRepository<Diary, Long> {

    fun findByUuid(uuid: String): Diary?

    fun findAllByUser(user: User, pageable: Pageable): Page<Diary>

    fun existsDiaryByUuid(uuid: String): Boolean

}