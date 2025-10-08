package com.anne.server.infrastructure.persistence.diary.mapper

import com.anne.server.domain.Diary
import com.anne.server.infrastructure.persistence.diary.entity.DiaryJpaEntity
import com.anne.server.infrastructure.persistence.user.entity.UserJpaEntity

object DiaryMapper {

    fun toEntity(
        domain: Diary,
        userRef: (Long) -> UserJpaEntity
    ): DiaryJpaEntity =
        DiaryJpaEntity(
            id = domain.id,
            emotion = domain.emotion,
            content = domain.content,
            uuid = domain.uuid,
            user = userRef(domain.userId)
        )

    fun toDomain(entity: DiaryJpaEntity): Diary =
        Diary(
            id = entity.id,
            emotion = entity.emotion,
            content = entity.content,
            uuid = entity.uuid,
            userId = entity.user.id!!,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )

}