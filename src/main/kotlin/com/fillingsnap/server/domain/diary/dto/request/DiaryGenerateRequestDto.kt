package com.fillingsnap.server.domain.diary.dto.request

data class DiaryGenerateRequestDto (

    val uuid: String,

    val textList: List<String>

) {
}