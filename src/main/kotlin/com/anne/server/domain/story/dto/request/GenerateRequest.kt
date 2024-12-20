package com.anne.server.domain.story.dto.request

import com.anne.server.global.validation.annotation.Uuid
import com.anne.server.global.validation.ValidationGroup
import jakarta.validation.constraints.NotBlank

data class GenerateRequest (

    @field:NotBlank(
        message = "UUID는 필수 값 입니다",
        groups = [ValidationGroup.NotNullGroup::class]
    )
    @field:Uuid(
        message = "올바르지 않은 UUID 값입니다",
        groups = [ValidationGroup.UuidGroup::class]
    )
    val uuid: String?,

    val textList: List<String>?

) {
}