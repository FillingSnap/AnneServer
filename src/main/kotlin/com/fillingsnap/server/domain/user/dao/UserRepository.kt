package com.fillingsnap.server.domain.user.dao

import com.fillingsnap.server.domain.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository: JpaRepository<User, Long> {

    fun findByUid(uid: String): User?

}