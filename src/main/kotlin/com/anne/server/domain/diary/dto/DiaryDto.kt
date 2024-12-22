package com.anne.server.domain.diary.dto

import com.anne.server.domain.diary.domain.Diary
import com.anne.server.domain.user.dto.UserDto
import java.time.LocalDateTime

data class DiaryDto (

    val id: Long?,

    val emotion: String,

    val content: String,

    val uuid: String,

    val user: UserDto,

    val createdAt: LocalDateTime,

    val updatedAt: LocalDateTime

) {

    companion object {
        fun toEntity(dto: DiaryDto): Diary {
            return Diary(
                id = dto.id,
                emotion = dto.emotion,
                content = dto.content,
                uuid = dto.uuid,
                user = UserDto.toEntity(dto.user)
            ).apply {
                this.createdAt = dto.createdAt
                this.updatedAt = dto.updatedAt
            }
        }

        fun fromEntity(diary: Diary): DiaryDto {
            return DiaryDto(
                id = diary.id,
                emotion = diary.emotion,
                content = diary.content,
                uuid = diary.uuid,
                user = UserDto.fromEntity(diary.user),
                createdAt = diary.createdAt,
                updatedAt = diary.updatedAt
            )
        }
    }

}