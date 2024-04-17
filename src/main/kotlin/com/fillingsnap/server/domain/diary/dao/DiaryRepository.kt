package com.fillingsnap.server.domain.diary.dao

import com.fillingsnap.server.domain.diary.domain.Diary
import com.fillingsnap.server.domain.user.domain.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface DiaryRepository: JpaRepository<Diary, Long> {

    fun findAllByUser(user: User): List<Diary>

    fun findAllByUser(user: User, pageable: Pageable): Page<Diary>

    fun existsDiaryByUuid(uuid: String): Boolean

}