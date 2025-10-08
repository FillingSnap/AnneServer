package com.anne.server.infrastructure.persistence.diary.repository

import com.anne.server.infrastructure.persistence.diary.entity.DiaryJpaEntity
import com.anne.server.infrastructure.persistence.user.entity.UserJpaEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface DiaryJpaRepository: JpaRepository<DiaryJpaEntity, Long> {

    fun existsByUuid(uuid: String): Boolean

    fun findByUuid(uuid: String): DiaryJpaEntity?

    fun findAllByUser(user: UserJpaEntity, pageable: Pageable): Page<DiaryJpaEntity>

}