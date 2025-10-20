package com.anne.server.domain

import java.time.LocalDateTime

data class Diary(

    val id: Long? = null,

    val emotion: String? = null,

    var content: String,

    val uuid: String,

    val userId: Long,

    val createdAt: LocalDateTime? = null,

    val updatedAt: LocalDateTime? = null

)