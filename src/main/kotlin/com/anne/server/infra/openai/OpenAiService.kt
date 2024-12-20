package com.anne.server.infra.openai

import com.anne.server.domain.diary.dao.DiaryRepository
import com.anne.server.domain.story.dao.StoryRepository
import com.anne.server.global.exception.CustomException
import com.anne.server.global.exception.ErrorCode
import com.anne.server.infra.amazon.service.S3Service
import com.anne.server.infra.openai.dto.ChatResponse
import com.anne.server.infra.openai.dto.SseResponse
import com.anne.server.infra.openai.dto.SseStatus
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.FileReader
import java.time.Duration
import java.util.*
import javax.imageio.ImageIO

@Service
class OpenAiService (

    @Value("\${openai.token}")
    private val token: String,

    @Value("\${json.diary}")
    private val diaryJson: String,

    private val restTemplate: RestTemplate,

    private val storyRepository: StoryRepository,

    private val diaryRepository: DiaryRepository,

    private val s3Service: S3Service

) {

    fun imageResize(image: String): ByteArray {
        val inputStream = s3Service.getObject(image)

        val sourceImage = ImageIO.read(inputStream)
        val width = 640
        val height = 640
        val resizeImage = BufferedImage(width, height, sourceImage.type)
        val g = resizeImage.createGraphics()
        g.drawImage(sourceImage, 0, 0, width, height, null)
        g.dispose()

        val byteArrayOutputStream = ByteArrayOutputStream()
        ImageIO.write(resizeImage, "png", byteArrayOutputStream)

        return byteArrayOutputStream.toByteArray()
    }

    fun request(requestImage: String): String {
        val headers = HttpHeaders()
        headers.set("Authorization", "Bearer $token")

        val jsonStr = "{\n" +
                "    \"model\": \"gpt-4o\", \n" +
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
            ChatResponse::class.java
        )
        if (response.statusCode != HttpStatus.OK) {
            throw CustomException(ErrorCode.CHAT_GPT_IMAGE_ANALYZE_FAILED)
        }

        return response.body!!.choices[0].message.content
    }

    // todo: 수정 필요
    fun openAi(uuid: String, delay: Long): Flux<SseResponse> {
        val parser = JSONParser()
        val systemContent = "- 키워드와 텍스트에 알맞은 일기를 작성하라.\n" +
                "- 키워드 순서에 따라 사건이 전개 되도록 작성하라.\n" +
                "- 일기에는 날짜가 포함되지 않게 작성하라.\n" +
                "- 예시로 입력된 일기의 말투를 참고하여 작성하라.\n" +
                "- 200자 내외로 작성하라."

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

        val text = diaryObject["content"] as String

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

        val storyList = storyRepository.findAllByUuid(uuid)

        val imageTextList = storyList.map {
            Pair(Base64.getEncoder().encodeToString(imageResize(it.image)), it.text)
        }

        var question = ""
        for (pair in imageTextList) {
            val result = request(pair.first)
            question += (result + ", 텍스트: [${pair.second}]\n")
        }

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

        val eventStream = client.post().uri("/chat/completions")
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $token")
            .bodyValue(obj)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .retrieve()
            .bodyToFlux(String::class.java)
            .delayElements(Duration.ofMillis(delay))
            .map {
                if (it == "[DONE]") {
                    SseResponse(
                        status = SseStatus.EOF,
                        content = result
                    )
                } else {
                    try {
                        val json = JSONParser().parse(it) as JSONObject
                        val choices = json["choices"] as JSONArray
                        val content = if (choices[0] != null) {
                            ((choices[0] as JSONObject)["delta"] as JSONObject)["content"] as String? ?: ""
                        } else {
                            ""
                        }
                        result += content
                        SseResponse(
                            status = SseStatus.SUCCESS,
                            content = content
                        )
                    } catch (e: Exception) {
                        SseResponse(
                            status = SseStatus.ERROR,
                            content = it
                        )
                    }
                }
            }
            .publishOn(Schedulers.boundedElastic())
            .doOnComplete {
                val newDiary = diaryRepository.findByUuid(uuid)
                    ?: throw CustomException(ErrorCode.DIARY_NOT_FOUND)
                newDiary.content = result
                diaryRepository.save(newDiary)

                storyList.forEach { it.diary = newDiary }
                storyRepository.saveAll(storyList)
            }.doOnError {
                SseResponse(
                    status = SseStatus.ERROR,
                    content = it.message
                )
            }

        return eventStream
    }

    fun test(delay: Long): Flux<SseResponse> {
        val result = "식사를 준비하며 스테이크와 야채를 손질하는 것은 항상 맛있는 시간이다. 스테이크를구워내는 소리와 야채가 향기로 " +
                "가득한 주방에서의 시간은 언제나 아름다운 순간이다. 나무 도마 위에서 재료들을 다듬고 음식을 만들어내는 과정은 맛있는 " +
                "요리를 만들어냄으로써 즐거움을 더해준다. 이런 소중한 시간을 보내며, 맛있는 음식을 먹을 때의 만족감은 무엇과도 바꿀 수 " +
                "없는 특별한 순간이다. 하늘을 바라보며 구름이 맑은 하늘을 가려주는 모습은 평화로움과 상쾌함을 느끼게 해준다. 구름 " +
                "사이로 비치는 햇살은 마치 세상의 모든 불평이 사라지고 마음이 맑아지는 듯한 기분을 안겨준다. 이런 풍경을 바라보며 " +
                "산뜻한 순간에는 감사함을 느끼며, 새로운 하루를 시작하고 싶어진다. 카페에서 손님들을 맞이하며 커피를 내리고 소품을 " +
                "정리하는 일상적인 모습은 편안함을 안겨준다. 카페 안에서 일어나는 일상의 소소한 순간들을 바라보며, 이곳에서 보내는 " +
                "시간은 언제나 특별하고 소중하다. 손님들과 함께하는 이 시간은 편안하고 소중한 것이다. 손님들과 이야기를 나누며 커피 한 " +
                "잔을 즐기는 것은 일상적인 활동이지만, 그 속에는 소중한 소품들과 이야기가 담겨있어 더욱 의미있는 시간이다. 카페 " +
                "안에서의 일상은 특별함을 발견할 수 있는 소중한 시간이다. 이런 소중한 순간들을 보면서, 일상 속에 숨겨진 아름다움을 " +
                "발견하고 감사함을 느낀다. "
        val list = result.split("")
        val eventStream = Flux.fromIterable(list + "[DONE]")
            .delayElements(Duration.ofMillis(delay))
            .map {
                if (it == "[DONE]") {
                    SseResponse(
                        status = SseStatus.EOF,
                        content = result
                    )
                } else {
                    SseResponse(
                        status = SseStatus.SUCCESS,
                        content = it ?: ""
                    )
                }
            }.doOnError {
                SseResponse(
                    status = SseStatus.ERROR,
                    content = it.message
                )
            }

        return eventStream
    }

}