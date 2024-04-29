package com.anne.server.domain.user.dao

import com.anne.server.domain.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository: JpaRepository<User, Long> {

    fun findByUidAndProvider(uid: String, provider: String): User?

}