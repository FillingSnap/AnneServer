package com.fillingsnap.server.domain.test.service

import com.aallam.openai.api.chat.*
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fillingsnap.server.domain.test.vo.AnalysisResultVo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.PropertySource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*


@Service
@PropertySource("classpath:naver.yml")
class TestService(

    @Value("\${X-NCP-APIGW-API-KEY-ID}")
    private val keyId: String,

    @Value("\${X-NCP-APIGW-API-KEY}")
    private val key: String

) {

    fun sentimentAnalysis(plainText: String): AnalysisResultVo {
        val restTemplate = RestTemplate()
        restTemplate.requestFactory = HttpComponentsClientHttpRequestFactory()

        val url = "https://naveropenapi.apigw.ntruss.com/sentiment-analysis/v1/analyze"

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.set("X-NCP-APIGW-API-KEY-ID", keyId)
        headers.set("X-NCP-APIGW-API-KEY", key)

        val requestBody = "{\"content\": \"$plainText\"}"
        val requestEntity = HttpEntity(requestBody, headers)

        val responseEntity = restTemplate.postForEntity(url, requestEntity, String::class.java)
        val jsonResponse = responseEntity.body

        val objectMapper = ObjectMapper().registerModule(
            KotlinModule.Builder().build()
        )

        return objectMapper.readValue(jsonResponse, AnalysisResultVo::class.java)
    }

    suspend fun openAI(): String {
        val token = "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"
        val openAI = OpenAI(token)

        // todo: 이미지를 로컬이 아닌 버킷에서 받아와야 함
        val imagePath = "C:\\Users\\heath\\Pictures\\Screenshots\\test.jpeg" // 파일경로
        val imageBytes = withContext(Dispatchers.IO) {
            Files.readAllBytes(Paths.get(imagePath))
        }
        val base64Image = Base64.getEncoder().encodeToString(imageBytes)

        val moodRequest = ChatCompletionRequest(
            model = ModelId("gpt-4-vision-preview"),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.User,
                    content = listOf(
                        TextPart("사진 안에 어떤 물체가 있고 어떤 분위기인지를 다음과 같은 형식으로 말해줘. [물체: [A, B, C], 분위기: [X, Y]]")
                    )
                ),
                ChatMessage(
                    role = ChatRole.User,
                    content = listOf(
                        ImagePart(
                            ImagePart.ImageURL(
                                url = "data:image/jpeg;base64,${base64Image}"
                            )
                        )
                    )
                ),
            ),
            maxTokens = 100
        )
        val moodCompletion: ChatCompletion = openAI.chatCompletion(moodRequest)

        val result = moodCompletion.choices[0].message.content
        val l = result?.indexOf("분위기")
        val question = """
        |주어진 분위기와 키워드를 참고하여 일기 형식의 글을 작성하라. 일기의 끝에 반드시 \e 토큰을 출력하라.
        |${result?.substring(l!!)}
        |키워드: ${result?.substring(4, l!! - 2)}
        |일기: """.trimMargin()

        var text = question
        for (i in 0 until 50) {
            val textResponse = openAI.chatCompletion(
                ChatCompletionRequest(
                    model = ModelId("gpt-3.5-turbo"),
                    messages = listOf(
                        ChatMessage(
                            role = ChatRole.User,
                            content = text
                        )
                    ),
                    temperature = 0.5,
                    topP = 1.0,
                    maxTokens = 16
                )
            )
            text += textResponse.choices[0].message.content
            // todo: 안드로이드에 웹소켓으로 전송해야 함
            println(textResponse.choices[0].message.content)
            println("${text.substring(question.length)}\r")
            if (text.substring(question.length).contains("\\e")) {
                break
            }
        }

        return text.substring(question.length)
    }

}