package com.anne.server.infrastructure.persistence.user.repository

import com.anne.server.infrastructure.persistence.user.entity.UserJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserJpaRepository: JpaRepository<UserJpaEntity, Long> {

    fun findByUidAndProvider(uid: String, provider: String): UserJpaEntity?

}