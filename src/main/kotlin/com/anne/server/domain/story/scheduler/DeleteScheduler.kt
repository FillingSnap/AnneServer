package com.anne.server.domain.story.scheduler

import com.anne.server.domain.story.dao.StoryRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class DeleteScheduler (

    private val storyRepository: StoryRepository

) {

    @Scheduled(cron = "0 0 6 * * *")
    fun deleteOldStories() {
        val weekAgo = LocalDateTime.now().minusDays(7)
        val oldStories = storyRepository.findAllByUpdatedAtBeforeAndDiaryIsNull(weekAgo)

        // todo: S3에서 삭제하는 건 어떻게?

        storyRepository.deleteAll(oldStories)
    }

}