package com.anne.server.domain.user.dto

import com.anne.server.domain.user.domain.User
import java.time.LocalDateTime

data class UserDto (

    val id: Long?,

    val name: String,

    val uid: String,

    val provider: String,

    val styleList: List<String>,

    val createdAt: LocalDateTime,

    val updatedAt: LocalDateTime

) {

    companion object {
        fun toEntity(dto: UserDto): User {
            return User(
                id = dto.id,
                name = dto.name,
                uid = dto.uid,
                provider = dto.provider,
            ).apply {
                this.createdAt = dto.createdAt
                this.updatedAt = dto.updatedAt
            }
        }

        fun fromEntity(user: User): UserDto {
            return UserDto(
                id = user.id,
                name = user.name,
                uid = user.uid,
                provider = user.provider,
                styleList = user.styleList,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt
            )
        }
    }

}