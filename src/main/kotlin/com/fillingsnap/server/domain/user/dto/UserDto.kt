package com.fillingsnap.server.domain.user.dto

import com.fillingsnap.server.domain.user.domain.User
import java.time.LocalDateTime

data class UserDto (

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