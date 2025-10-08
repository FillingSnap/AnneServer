package com.anne.server.application.diary.port.`in`

import com.anne.server.domain.Diary
import reactor.core.publisher.Flux

interface DiaryGenerateUseCase {

    fun streamDiary(userId: Long, uuid: String): Flux<String>

    fun generateDiary(userId: Long, uuid: String, content: String): Diary

}