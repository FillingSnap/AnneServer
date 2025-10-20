package com.anne.server.domain

import java.time.LocalDateTime

data class Story(

    val id: Long? = null,

    val text: String,

    val image: String,

    val uuid: String,

    val userId: Long,

    var diaryId: Long? = null,

    val createdAt: LocalDateTime? = null,

    val updatedAt: LocalDateTime? = null

)