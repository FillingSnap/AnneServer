package com.fillingsnap.server.infra.openai

import com.aallam.openai.api.chat.*
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.fillingsnap.server.global.config.websocket.WebSocketResponseDto
import com.fillingsnap.server.global.config.websocket.WebSocketStatus
import com.fillingsnap.server.infra.oracle.ObjectStorageService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.stereotype.Service

@Service
class OpenAiService (

    @Value("\${openai.token}")
    private val token: String,

    private val sendingOperations: SimpMessageSendingOperations

) {

    private val client = OpenAI(token)

    suspend fun request(image: String): String? {
        val moodRequest = ChatCompletionRequest(
            model = ModelId("gpt-4-vision-preview"),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.User,
                    content = listOf(
                        TextPart(
                            "사진의 키워드와 해당 사진이 어떤 분위기인지를 다음과 같은 형식으로 말하시오. " +
                            "[키워드: [A, B, C], 분위기: [X, Y]]"
                        ),
                        ImagePart(
                            ImagePart.ImageURL(
                                url = "data:image/jpeg;base64,${image}"
                            )
                        )
                    )
                ),
            ),
            maxTokens = 100
        )
        val moodCompletion: ChatCompletion = client.chatCompletion(moodRequest)

        return moodCompletion.choices[0].message.content
    }

    suspend fun openAi(id: Long, imageTextList: List<Pair<String, String>>): String {
        var question =
            "주어진 키워드와 텍스트, 분위기를 참고하여 일기 형식의 글을 작성하라. 일기의 끝에 반드시 '&' 라는 토큰을 출력하라.\n" +
            "키워드: [커피숍, 책]\n" +
            "분위기: [평온함, 행복, 감사함]\n" +
            "텍스트: 카페에서 커피 한잔 ㅎㅎ\n\n" +
            "키워드: [하늘, 구름, 건물]\n" +
            "분위기: [평화로운, 밝은]\n" +
            "텍스트: \n\n" +
            "키워드: [스시, 일식, 해산물]\n" +
            "분위기: [맛있어 보임, 전통적]\n" +
            "텍스트: 친구들이랑 저녁 @문태진\n\n" +
            "일기: 카페에서 커피 한 잔을 즐기며 책을 읽는 시간은 언제나 평온함과 행복을 선사해준다. " +
            "커피숍의 아늑한 분위기와 함께 책 속으로 빠져들면, 일상의 소란과 스트레스가 멀어지고 마음이 가라앉는다. " +
            "이런 소중한 시간들을 갖게 해주는 것에 감사함을 느낀다. " +
            "하늘을 바라보며 건물과 구름이 어우러진 풍경은 평화로움과 밝음을 안겨준다. " +
            "구름 사이로 비치는 햇살은 마치 우리 안에 있는 작은 희망이 피어나는 것처럼 느껴진다. " +
            "이런 순간들을 만끽하며 세상의 아름다움에 다시 한번 감사함을 느낀다. " +
            "친구들과 함께한 저녁 @문태진은 맛있어 보이는 스시와 해산물로 가득찬 전통적인 일식 요리를 맛볼 수 있었다. " +
            "함께한 시간은 즐겁고 소중한 추억으로 남을 것이다. " +
            "친구들과 함께한 이 특별한 저녁에 감사함을 느끼며, 내일을 기대한다. &\n\n"

        for (pair in imageTextList) {
            val result = request(pair.first)!!
            val l = result.indexOf("분위기")
            question +=
                "키워드: ${result.substring(5, l - 2)}\n" +
                "분위기: ${result.substring(l + 5)}\n" +
                "텍스트: ${pair.second}\n\n"
        }
        question += "일기: "

        var text = question
        for (i in 0 until 50) {
            val textResponse = client.chatCompletion(
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
            val response = WebSocketResponseDto(
                status = WebSocketStatus.SUCCESS,
                content = textResponse.choices[0].message.content
            )

            sendingOperations.convertAndSend("/queue/channel/${id}", response)
            if (text.substring(question.length).contains("&")) {
                break
            }
        }

        return text.substring(question.length)
    }

}