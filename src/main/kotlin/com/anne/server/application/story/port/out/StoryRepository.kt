package com.anne.server.application.story.port.out

import com.anne.server.domain.Story

interface StoryRepository {

    fun existsByUuid(uuid: String): Boolean

    fun findById(id: Long): Story?

    fun findAllByUuid(uuid: String): List<Story>

    fun save(story: Story): Story

    fun saveAll(storyList: List<Story>): List<Story>

}