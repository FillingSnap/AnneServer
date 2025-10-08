package com.anne.server.infrastructure.persistence.story.mapper

import com.anne.server.domain.Story
import com.anne.server.infrastructure.persistence.diary.entity.DiaryJpaEntity
import com.anne.server.infrastructure.persistence.story.entity.StoryJpaEntity
import com.anne.server.infrastructure.persistence.user.entity.UserJpaEntity

object StoryMapper {

    fun toEntity(
        domain: Story,
        userRef: (Long) -> UserJpaEntity,
        diaryRef: (Long) -> DiaryJpaEntity
    ): StoryJpaEntity =
        StoryJpaEntity(
            id = domain.id,
            text = domain.text,
            image = domain.image,
            uuid = domain.uuid,
            user = userRef(domain.userId),
            diary = domain.diaryId?.let { diaryRef(it) },
        )

    fun toDomain(entity: StoryJpaEntity): Story =
        Story(
            id = entity.id,
            text = entity.text,
            image = entity.image,
            uuid = entity.uuid,
            userId = entity.user.id!!,
            diaryId = entity.diary?.id,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )

}