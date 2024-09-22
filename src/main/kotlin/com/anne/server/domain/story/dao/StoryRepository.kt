package com.anne.server.domain.story.dao

import com.anne.server.domain.story.domain.Story
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface StoryRepository: JpaRepository<Story, Long> {

    fun findAllByUuid(uuid: String): List<Story>

    fun existsStoryByUuid(uuid: String): Boolean

    fun findAllByUpdatedAtBefore(time: LocalDateTime): List<Story>

}