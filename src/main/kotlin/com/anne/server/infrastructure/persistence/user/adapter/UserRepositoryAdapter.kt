package com.anne.server.infrastructure.persistence.user.adapter

import com.anne.server.application.user.port.out.UserRepository
import com.anne.server.domain.User
import com.anne.server.infrastructure.persistence.user.mapper.UserMapper
import com.anne.server.infrastructure.persistence.user.repository.UserJpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class UserRepositoryAdapter(

    private val userJpaRepository: UserJpaRepository

): UserRepository {

    override fun findById(id: Long): User? =
        userJpaRepository.findByIdOrNull(id)
            ?.let { UserMapper.toDomain(it) }

    override fun findByUidAndProvider(uid: String, provider: String): User? =
        userJpaRepository.findByUidAndProvider(uid, provider)
            ?.let { UserMapper.toDomain(it) }

    override fun save(user: User): User =
        UserMapper.toDomain(
            userJpaRepository.save(
                UserMapper.toEntity(user)
            )
        )

}