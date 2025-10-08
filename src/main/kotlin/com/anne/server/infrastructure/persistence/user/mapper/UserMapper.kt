package com.anne.server.infrastructure.persistence.user.mapper

import com.anne.server.domain.User
import com.anne.server.infrastructure.persistence.user.entity.UserJpaEntity

object UserMapper {

    fun toEntity(domain: User): UserJpaEntity =
        UserJpaEntity(
            id = domain.id,
            name = domain.name,
            uid = domain.uid,
            provider = domain.provider,
            styleList = domain.styleList
        )

    fun toDomain(entity: UserJpaEntity): User =
        User(
            id = entity.id,
            name = entity.name,
            uid = entity.uid,
            provider = entity.provider,
            styleList = entity.styleList
        )

}