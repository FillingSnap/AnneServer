package com.anne.server.domain.story.dto

import com.anne.server.domain.diary.dto.DiaryDto
import com.anne.server.domain.story.domain.Story
import com.anne.server.domain.user.dto.UserDto
import java.time.LocalDateTime

data class StoryDto (

    val id: Long?,

    val text: String,

    val image: String,

    val uuid: String,

    val user: UserDto,

    val diary: DiaryDto?,

    val createdAt: LocalDateTime,

    val updatedAt: LocalDateTime

) {

    companion object {
        fun toEntity(dto: StoryDto): Story {
            return Story(
                id = dto.id,
                text = dto.text,
                image = dto.image,
                uuid = dto.uuid,
                user = UserDto.toEntity(dto.user),
                diary = dto.diary?.let { DiaryDto.toEntity(it) }
            ).apply {
                this.createdAt = dto.createdAt
                this.updatedAt = dto.updatedAt
            }
        }

        fun fromEntity(story: Story): StoryDto {
            return StoryDto(
                id = story.id,
                text = story.text,
                image = story.image,
                uuid = story.uuid,
                user = UserDto.fromEntity(story.user),
                diary = story.diary?.let { DiaryDto.fromEntity(it) },
                createdAt = story.createdAt,
                updatedAt = story.updatedAt
            )
        }
    }

}