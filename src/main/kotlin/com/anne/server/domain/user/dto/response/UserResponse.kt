package com.anne.server.domain.user.dto.response

import com.anne.server.domain.user.domain.User
import java.time.LocalDateTime

data class UserResponse (

    val id: Long,

    val name: String,

    val uid: String,

    val provider: String,

    val createdAt: LocalDateTime,

    val updatedAt: LocalDateTime

) {

    constructor(user: User): this(
        user.id!!,
        user.name,
        user.uid,
        user.provider,
        user.createdAt,
        user.updatedAt
    )

}