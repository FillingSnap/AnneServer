package com.anne.server.application.diary.port.out

import com.anne.server.application.diary.dto.DiaryStream
import com.anne.server.application.diary.dto.ImageTextDto
import reactor.core.publisher.Flux

interface DiaryStreamGenerator {

    fun diaryStream(imageTextList: List<ImageTextDto>): Flux<DiaryStream>

}