package com.fillingsnap.server.domain.story.dto

import com.fillingsnap.server.domain.story.domain.Story
import java.time.LocalDateTime

data class SimpleStudyDto (

    val id: Long,

    val text: String,

    val image: String,

    val createdAt: LocalDateTime,

    val updatedAt: LocalDateTime

) {

    constructor(story: Story) : this(
        story.id,
        story.text,
        story.image,
        story.createdAt,
        story.updatedAt
    )

}