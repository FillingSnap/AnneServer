package com.anne.server.infra.openai.config

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.FileReader

@Component
class JsonConfig (

    @Value("\${json.diary}")
    private val diaryJson: String

) {

    private final val diaryList = JSONParser().parse(FileReader(diaryJson)) as JSONArray

    fun getImageAnalyzeRequestJson(requestImage: String): JSONObject {
        val messages = JSONArray().apply {
            add(
                JSONObject().apply {
                    put("role", "user")
                    put("content", JSONArray().apply {
                        add(JSONObject().apply {
                            put("text", "사진 안에 어떤 물체가 있고 어떤 분위기인지를 다음과 같은 형식으로 말해줘. [분위기: [A, B, C], 키워드: [X, Y]]")
                            put("type", "text")
                        })
                        add(JSONObject().apply {
                            put("type", "image_url")
                            put("image_url", JSONObject().apply {
                                put("url", "data:image/jpeg;base64,$requestImage")
                            })
                        })
                    })
                })
        }

        val obj = JSONObject().apply {
            put("model", "gpt-4o")
            put("messages", messages)
            put("max_tokens", 100)
        }

        return obj
    }

    fun getGenerateDiaryRequestJson(question: String): JSONObject {
        val systemContent = "- 키워드와 텍스트에 알맞은 일기를 작성하라.\n" +
                "- 키워드 순서에 따라 사건이 전개 되도록 작성하라.\n" +
                "- 일기에는 날짜가 포함되지 않게 작성하라.\n" +
                "- 예시로 입력된 일기의 말투를 참고하여 작성하라.\n" +
                "- 200자 내외로 작성하라."

        val diaryExample = diaryList[0] as JSONObject

        // 예시 분위기 추출
        var atmosphereList = emptyList<String>()
        val atmosphereJSONArray = diaryExample["atmosphere"] as JSONArray
        for (i: Int in atmosphereJSONArray.indices) {
            atmosphereList = atmosphereList.plus(atmosphereJSONArray[i].toString())
        }

        // 예시 키워드 추출
        var keywordList = emptyList<String>()
        val keywordJSONArray = diaryExample["keyword"] as JSONArray
        for (i: Int in keywordJSONArray.indices) {
            keywordList = keywordList.plus(keywordJSONArray[i].toString())
        }

        // 예시 내용 추출
        val text = diaryExample["content"] as String

        // 예시 입력
        var userContent = "분위기: ["
        for (atmosphere in atmosphereList) {
            userContent += ("$atmosphere, ")
        }
        userContent += "\b\b], 키워드: ["
        for (keyword in keywordList) {
            userContent += ("$keyword, ")
        }
        userContent += "\b\b], 텍스트: [$text]"

        val responseContent = diaryExample["content"] as String

        val messages = JSONArray().apply {
            add(JSONObject().apply {
                put("role", "system")
                put("content", systemContent)
            })
            add(JSONObject().apply {
                put("role", "user")
                put("content", userContent)
            })
            add(JSONObject().apply {
                put("role", "assistant")
                put("content", responseContent)
            })
            add(JSONObject().apply {
                put("role", "user")
                put("content", question)
            })
        }

        val obj = JSONObject().apply {
            put("model", "gpt-4")
            put("messages", messages)
            put("temperature", 0.9)
            put("stream", true)
        }

        return obj
    }

}