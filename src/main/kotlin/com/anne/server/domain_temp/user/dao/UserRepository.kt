package com.anne.server.domain_temp.user.dao

import com.anne.server.domain_temp.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository: JpaRepository<User, Long> {

    fun findByUidAndProvider(uid: String, provider: String): User?

}