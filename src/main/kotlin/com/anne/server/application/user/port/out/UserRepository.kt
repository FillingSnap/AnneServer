package com.anne.server.application.user.port.out

import com.anne.server.domain.User

interface UserRepository {

    fun findById(id: Long): User?

    fun findByUidAndProvider(uid: String, provider: String): User?

    fun save(user: User): User

}