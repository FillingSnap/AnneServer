package com.fillingsnap.server.domain.user.dto

import com.fillingsnap.server.domain.diary.dto.SimpleDiaryDto
import com.fillingsnap.server.domain.user.domain.User

data class UserDto (

    val id: Long,

    val name: String,

    val uid: String,

    val provider: String,

    val diaryList: List<SimpleDiaryDto>

) {

    constructor(user: User): this(
        user.id!!, user.name, user.uid, user.provider, user.diaryList.map { SimpleDiaryDto(it) }
    )

}