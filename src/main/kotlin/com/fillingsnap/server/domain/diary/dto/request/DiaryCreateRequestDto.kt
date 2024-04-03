package com.fillingsnap.server.domain.diary.dto.request

import com.fillingsnap.server.global.validation.Uuid
import com.fillingsnap.server.global.validation.ValidationGroup
import jakarta.validation.constraints.NotBlank

data class DiaryCreateRequestDto (

    @field:NotBlank(
        message = "UUID는 필수 값 입니다",
        groups = [ValidationGroup.NotNullGroup::class]
    )
    @field:Uuid(
        message = "올바르지 않은 UUID 값입니다",
        groups = [ValidationGroup.UuidGroup::class]
    )
    val uuid: String?,

    @field:NotBlank(
        message = "일기 내용은 필수 값 입니다",
        groups = [ValidationGroup.NotNullGroup::class]
    )
    val content: String?

) {
}