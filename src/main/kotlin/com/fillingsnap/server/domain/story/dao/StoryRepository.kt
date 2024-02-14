package com.fillingsnap.server.domain.story.dao

import com.fillingsnap.server.domain.story.domain.Story
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface StoryRepository: JpaRepository<Story, Long> {

    fun findAllByCreatedAtBetween(startDateTime: LocalDateTime, endDateTime: LocalDateTime): List<Story>

}