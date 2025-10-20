package com.anne.server.application.story.service

import com.anne.server.application.diary.port.out.DiaryRepository
import com.anne.server.application.story.port.`in`.StoryUseCase
import com.anne.server.application.story.port.out.ObjectStorage
import com.anne.server.application.story.port.out.StoryRepository
import com.anne.server.domain.Story
import com.anne.server.common.exception.ErrorCode
import com.anne.server.common.exception.CustomException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class StoryService(

    private val storyRepository: StoryRepository,

    private val diaryRepository: DiaryRepository,

    private val objectStorage: ObjectStorage

) : StoryUseCase {

    @Transactional(readOnly = true)
    override fun getStory(userId: Long, storyId: Long): Story =
        storyRepository.findById(storyId)
            ?.apply {
                if (this.userId != userId)
                    throw CustomException(ErrorCode.NOT_YOUR_STORY)
            }
            ?: throw CustomException(ErrorCode.STORY_NOT_FOUND)

    @Transactional
    override fun postStoryList(
        userId: Long,
        uuid: String,
        imageList: List<MultipartFile>?,
        textList: List<String>?
    ): List<Story> {
        if (diaryRepository.existsByUuid(uuid))
            throw CustomException(ErrorCode.ALREADY_EXIST_UUID)

        if (imageList == null || textList == null) {
            throw CustomException(ErrorCode.IMAGE_TEXT_REQUIRED)
        } else if (imageList.size != textList.size) {
            throw CustomException(ErrorCode.IMAGE_TEXT_NOT_MATCHING)
        }

        val storyList = arrayListOf<Story>()
        val savedImageList =  arrayListOf<String>()

        for (i in imageList.indices) {
            val image: String = try {
                objectStorage.uploadObject(userId, imageList[i])
            } catch (_: Exception) {
                for (image in savedImageList) {
                    objectStorage.deleteObject(image)
                }
                throw CustomException(ErrorCode.IMAGE_SAVE_ERROR)
            }
            savedImageList.add(image)
            val story = Story(
                text = textList[i],
                image = image,
                uuid = uuid,
                userId = userId
            )
            storyList.add(story)
        }

        return storyRepository.saveAll(storyList)
    }

}