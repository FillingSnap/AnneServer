package com.anne.server.domain.diary.dto.request

import com.anne.server.global.validation.ValidationGroup
import jakarta.validation.constraints.NotBlank

data class UpdateRequest (

    @field:NotBlank(
        message = "일기 내용은 필수 값 입니다",
        groups = [ValidationGroup.NotNullGroup::class]
    )
    val content: String

) {
}
