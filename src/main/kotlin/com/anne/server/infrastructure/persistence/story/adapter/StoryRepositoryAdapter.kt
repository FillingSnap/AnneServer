package com.anne.server.infrastructure.persistence.story.adapter

import com.anne.server.application.story.port.out.StoryRepository
import com.anne.server.domain.Story
import com.anne.server.infrastructure.persistence.diary.entity.DiaryJpaEntity
import com.anne.server.infrastructure.persistence.story.mapper.StoryMapper
import com.anne.server.infrastructure.persistence.story.repository.StoryJpaRepository
import com.anne.server.infrastructure.persistence.user.entity.UserJpaEntity
import jakarta.persistence.EntityManager
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class StoryRepositoryAdapter(

    private val storyJpaRepository: StoryJpaRepository,

    private val em: EntityManager

): StoryRepository {

    private val userRef: (Long) -> UserJpaEntity = {
        em.getReference(UserJpaEntity::class.java, it)
    }

    private val diaryRef: (Long) -> DiaryJpaEntity = {
        em.getReference(DiaryJpaEntity::class.java, it)
    }

    override fun existsByUuid(uuid: String): Boolean =
        storyJpaRepository.existsByUuid(uuid)

    override fun findById(id: Long): Story? =
        storyJpaRepository.findByIdOrNull(id)
            ?.let { StoryMapper.toDomain(it) }

    override fun findAllByUuid(uuid: String): List<Story> =
        storyJpaRepository.findAllByUuid(uuid)
            .map { StoryMapper.toDomain(it) }

    override fun save(story: Story): Story =
        StoryMapper.toDomain(
            storyJpaRepository.save(
                StoryMapper.toEntity(story, userRef, diaryRef)
            )
        )

    override fun saveAll(storyList: List<Story>): List<Story> =
        storyJpaRepository.saveAll(
            storyList.map { StoryMapper.toEntity(it, userRef, diaryRef) }
        ).map { StoryMapper.toDomain(it) }

}