package com.anne.server.domain.diary.dto.response

import com.anne.server.domain.diary.domain.Diary
import com.anne.server.domain.story.dto.response.StorySimpleResponseDto
import java.time.LocalDateTime

data class DiaryWithStoryResponseDto (

    val id: Long?,

    val emotion: String,

    val content: String,

    val uuid: String,

    val userId: Long,

    val createdAt: LocalDateTime,

    val updatedAt: LocalDateTime,

    val storyList: List<StorySimpleResponseDto>

) {

    constructor(diary: Diary): this(
        diary.id,
        diary.emotion,
        diary.content,
        diary.uuid,
        diary.user.id!!,
        diary.createdAt,
        diary.updatedAt,
        diary.storyList.map { StorySimpleResponseDto(it) }
    )

}