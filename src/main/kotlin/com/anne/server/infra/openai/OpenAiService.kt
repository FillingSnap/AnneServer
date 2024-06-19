package com.anne.server.infra.openai

import com.anne.server.infra.openai.dto.ChatResponseDto
import com.anne.server.global.websocket.dto.WebSocketResponseDto
import com.anne.server.global.websocket.WebSocketStatus
import com.anne.server.infra.openai.dto.SseResponseDto
import com.anne.server.infra.redis.RedisDao
import io.netty.channel.EventLoopGroup
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import java.io.FileReader
import java.time.Duration

@Service
class OpenAiService (

    @Value("\${openai.token}")
    private val token: String,

    @Value("\${json.diary}")
    private val diaryJson: String,

    @Value("\${json.mbti}")
    private val mbtiJson: String,

    private val sendingOperations: SimpMessageSendingOperations,

    private val restTemplate: RestTemplate,

    private val redisDao: RedisDao

) {

    fun request(requestImage: String): String {
        val headers = HttpHeaders()
        headers.set("Authorization", "Bearer $token")

        val jsonStr = "{\n" +
                "    \"model\": \"gpt-4-vision-preview\", \n" +
                "    \"messages\": [\n" +
                "        {\n" +
                "            \"role\": \"user\",\n" +
                "            \"content\": [\n" +
                "                {\n" +
                "                    \"text\": \"사진 안에 어떤 물체가 있고 어떤 분위기인지를 다음과 같은 형식으로 말해줘. " +
                "[분위기: [A, B, C], 키워드: [X, Y]]\",\n" +
                "                    \"type\": \"text\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"type\": \"image_url\", \n" +
                "                    \"image_url\": {\n" +
                "                        \"url\": \"data:image/jpeg;base64,$requestImage\"\n" +
                "                    }\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    ], \n" +
                "    \"max_tokens\": 100\n" +
                "}"

        val jsonParser = JSONParser()
        val obj = jsonParser.parse(jsonStr) as JSONObject

        val entity = HttpEntity(obj, headers)

        val response = restTemplate.exchange(
            "https://api.openai.com/v1/chat/completions",
            HttpMethod.POST,
            entity,
            ChatResponseDto::class.java
        )

        return response.body!!.choices[0].message.content
    }

    fun openAi(uuid: String, id: Long, imageTextList: List<Pair<String, String>>): String {
        val parser = JSONParser()
        val mbtiReader = FileReader("./src/main/resources/json/mbti.json")
        val mbtiObject = parser.parse(mbtiReader) as JSONObject

        val mbti = "INFP"
        val systemContent = "당신의 MBTI는 ${mbti}이다. ${mbtiObject[mbti]}. 또한 당신은 세상에서 글을 잘 쓰는 작가이다. " +
                "주어진 분위기와 키워드를 참고하여 일기를 작성하라."

        val diaryReader = FileReader("./src/main/resources/json/diary.json")
        val diaryArray = parser.parse(diaryReader) as JSONArray
        val diary = 0
        val diaryObject = diaryArray[diary] as JSONObject

        var atmosphereList = emptyList<String>()
        val atmosphereJSONArray = diaryObject["atmosphere"] as JSONArray
        for (i: Int in atmosphereJSONArray.indices) {
            atmosphereList = atmosphereList.plus(atmosphereJSONArray[i].toString())
        }

        var keywordList = emptyList<String>()
        val keywordJSONArray = diaryObject["keyword"] as JSONArray
        for (i: Int in keywordJSONArray.indices) {
            keywordList = keywordList.plus(keywordJSONArray[i].toString())
        }

        val text = diaryObject["text"] as String

        var userContent = "분위기: ["
        for (atmosphere in atmosphereList) {
            userContent += ("$atmosphere, ")
        }
        userContent += "\b\b], 키워드: ["
        for (keyword in keywordList) {
            userContent += ("$keyword, ")
        }
        userContent += "\b\b], 텍스트: [$text]"

        val responseContent = diaryObject["content"] as String

        var question = ""
        for (pair in imageTextList) {
            val result = request(pair.first)
            question += (result + ", 텍스트: [${pair.second}]\n")
        }

        var result = ""

        val client = WebClient.create("https://api.openai.com/v1")
        val prompt = "{\n" +
                "    \"model\": \"gpt-4\",\n" +
                "    \"messages\": [\n" +
                "        {\n" +
                "            \"role\": \"system\",\n" +
                "            \"content\": \"$systemContent\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"role\": \"user\",\n" +
                "            \"content\": \"$userContent\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"role\": \"assistant\",\n" +
                "            \"content\": \"$responseContent\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"role\": \"user\",\n" +
                "            \"content\": \"$question\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"temperature\": 0.9,\n" +
                "    \"stream\": true\n" +
                "}"

        val jsonParser = JSONParser()
        val obj = jsonParser.parse(prompt) as JSONObject

        val eventStream = client.post().uri("/chat/completions")
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $token")
            .bodyValue(obj)
            .retrieve()
            .bodyToFlux(String::class.java)
            .publishOn(Schedulers.boundedElastic())
            .doOnNext { response ->
                if (response != "[DONE]") {
                    val json = JSONParser().parse(response) as JSONObject
                    val choices = json["choices"] as JSONArray
                    val content = ((choices[0] as JSONObject)["delta"] as JSONObject)["content"] as String?

                    if (content == null) {
                        sendingOperations.convertAndSend(
                            "/queue/channel/${id}",
                            WebSocketResponseDto(
                                status = WebSocketStatus.EOF,
                                content = result
                            )
                        )
                        redisDao.setValues(uuid, result, Duration.ofMillis(604800000L))
                    } else {
                        sendingOperations.convertAndSend(
                            "/queue/channel/${id}",
                            WebSocketResponseDto(
                                status = WebSocketStatus.SUCCESS,
                                content = content
                            )
                        )
                        result += content
                    }
                    Thread.sleep(500)
                }
            }

        eventStream.subscribe()

        return result
    }

    fun test(uuid: String, id: Long): Flux<SseResponseDto> {
        val parser = JSONParser()
        val mbtiReader = FileReader(mbtiJson)
        val mbtiObject = parser.parse(mbtiReader) as JSONObject

        val mbti = "INFP"
        val systemContent = "당신의 MBTI는 ${mbti}이다. ${mbtiObject[mbti]}. 또한 당신은 세상에서 글을 잘 쓰는 작가이다. " +
                "주어진 분위기와 키워드를 참고하여 일기를 작성하라."

        val diaryReader = FileReader(diaryJson)
        val diaryArray = parser.parse(diaryReader) as JSONArray
        val diary = 0
        val diaryObject = diaryArray[diary] as JSONObject

        var atmosphereList = emptyList<String>()
        val atmosphereJSONArray = diaryObject["atmosphere"] as JSONArray
        for (i: Int in atmosphereJSONArray.indices) {
            atmosphereList = atmosphereList.plus(atmosphereJSONArray[i].toString())
        }

        var keywordList = emptyList<String>()
        val keywordJSONArray = diaryObject["keyword"] as JSONArray
        for (i: Int in keywordJSONArray.indices) {
            keywordList = keywordList.plus(keywordJSONArray[i].toString())
        }

        val text = diaryObject["text"] as String

        var userContent = "분위기: ["
        for (atmosphere in atmosphereList) {
            userContent += ("$atmosphere, ")
        }
        userContent += "\b\b], 키워드: ["
        for (keyword in keywordList) {
            userContent += ("$keyword, ")
        }
        userContent += "\b\b], 텍스트: [$text]"

        val responseContent = diaryObject["content"] as String

        val question = "분위기: [평온함, 행복, 감사함], 텍스트: [커피숍, 책, 피크닉, 친구, 가족]"

        val client = WebClient.create("https://api.openai.com/v1")
        val prompt = "{\n" +
                "    \"model\": \"gpt-4\",\n" +
                "    \"messages\": [\n" +
                "        {\n" +
                "            \"role\": \"system\",\n" +
                "            \"content\": \"$systemContent\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"role\": \"user\",\n" +
                "            \"content\": \"$userContent\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"role\": \"assistant\",\n" +
                "            \"content\": \"$responseContent\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"role\": \"user\",\n" +
                "            \"content\": \"$question\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"temperature\": 0.9,\n" +
                "    \"stream\": true\n" +
                "}"

        val jsonParser = JSONParser()
        val obj = jsonParser.parse(prompt) as JSONObject

        var result = ""
        var seq = 0

        val eventStream = client.post().uri("/chat/completions")
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $token")
            .bodyValue(obj)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .retrieve()
            .bodyToFlux(String::class.java)
            .delayElements(Duration.ofMillis(500))
            .map {
                if (it == "[DONE]") {
                    SseResponseDto(
                        seq = seq++,
                        status = WebSocketStatus.EOF,
                        content = result
                    )
                } else {
                    val json = JSONParser().parse(it) as JSONObject
                    val choices = json["choices"] as JSONArray
                    val content = ((choices[0] as JSONObject)["delta"] as JSONObject)["content"] as String?
                    println(content)
                    result += content
                    SseResponseDto(
                        seq = seq++,
                        status = WebSocketStatus.SUCCESS,
                        content = content ?: ""
                    )
                }
            }

        return eventStream
    }

}