package com.anne.server.infra.openai.service

import com.anne.server.infra.openai.dao.OpenAiRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class OpenAiService (

    private val openAiRepository: OpenAiRepository

) {

    fun generateDiary(imageTextList: List<Pair<String, String>>, delay: Long): Flux<String> {
        var question = ""
        for (pair in imageTextList) {
            val result = openAiRepository.imageAnalyze(pair.first)
            question += (result + ", 텍스트: [${pair.second}]\n")
        }

        return openAiRepository.generateDiary(question, delay)
    }

}