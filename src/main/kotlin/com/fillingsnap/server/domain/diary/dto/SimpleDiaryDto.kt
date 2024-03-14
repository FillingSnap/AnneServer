package com.fillingsnap.server.domain.diary.dto

import com.fillingsnap.server.domain.diary.domain.Diary
import java.time.LocalDateTime

data class SimpleDiaryDto(

    val id: Long?,

    val emotion: String,

    val content: String,

    val userId: Long,

    val createdAt: LocalDateTime,

    val updatedAt: LocalDateTime

) {

    constructor(diary: Diary): this(
        diary.id,
        diary.emotion,
        diary.content,
        diary.user.id!!,
        diary.createdAt,
        diary.updatedAt
    )

}