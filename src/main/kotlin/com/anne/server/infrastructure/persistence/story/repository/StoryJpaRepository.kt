package com.anne.server.infrastructure.persistence.story.repository

import com.anne.server.infrastructure.persistence.story.entity.StoryJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface StoryJpaRepository: JpaRepository<StoryJpaEntity, Long> {

    fun existsByUuid(uuid: String): Boolean

    fun findAllByUuid(uuid: String): List<StoryJpaEntity>

}