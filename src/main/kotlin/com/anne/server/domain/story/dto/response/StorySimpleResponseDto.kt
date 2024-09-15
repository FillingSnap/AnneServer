package com.anne.server.domain.story.dto.response

import com.anne.server.domain.story.domain.Story
import java.time.LocalDateTime

data class StorySimpleResponseDto (

    val id: Long?,

    val text: String,

    val image: String,

    val uuid: String,

    val createdAt: LocalDateTime,

    val updatedAt: LocalDateTime

) {

    constructor(story: Story) : this(
        story.id,
        story.text,
        story.image,
        story.uuid,
        story.createdAt,
        story.updatedAt
    )

}