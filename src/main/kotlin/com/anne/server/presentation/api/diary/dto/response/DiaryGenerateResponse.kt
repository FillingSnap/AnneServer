package com.anne.server.presentation.api.diary.dto.response

data class DiaryGenerateResponse (

    val status: DiaryGenerateStatus,

    val content: String?

)