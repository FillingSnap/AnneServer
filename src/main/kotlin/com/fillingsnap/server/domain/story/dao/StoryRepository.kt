package com.fillingsnap.server.domain.story.dao

import com.fillingsnap.server.domain.story.domain.Story
import org.springframework.data.jpa.repository.JpaRepository

interface StoryRepository: JpaRepository<Story, Long> {
}