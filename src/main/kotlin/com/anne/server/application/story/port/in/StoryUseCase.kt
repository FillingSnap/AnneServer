package com.anne.server.application.story.port.`in`

import com.anne.server.domain.Story
import org.springframework.web.multipart.MultipartFile

interface StoryUseCase {

    fun getStory(userId: Long, storyId: Long): Story

    fun postStoryList(userId: Long, uuid: String, imageList: List<MultipartFile>?, textList: List<String>?): List<Story>

}