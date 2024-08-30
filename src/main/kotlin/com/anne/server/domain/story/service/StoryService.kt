package com.anne.server.domain.story.service

import com.anne.server.domain.story.dao.StoryRepository
import com.anne.server.domain.story.domain.Story
import com.anne.server.domain.story.dto.response.StudySimpleResponseDto
import com.anne.server.domain.user.domain.User
import com.anne.server.global.exception.CustomException
import com.anne.server.global.exception.ErrorCode
import com.anne.server.infra.amazon.AwsS3Service
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile


@Service
class StoryService(

    private val storyRepository: StoryRepository,

    private val awsS3Service: AwsS3Service,

) {

    fun getStoryById(id: Long): StudySimpleResponseDto {
        val study = storyRepository.findByIdOrNull(id)
            ?: throw CustomException(ErrorCode.STORY_NOT_FOUND)

        val user = SecurityContextHolder.getContext().authentication.principal as User

        // 해당 스토리의 소유자가 아닌 경우
        if (user.id!! != study.user.id!!) {
            throw CustomException(ErrorCode.NOT_YOUR_STORY)
        }

        return StudySimpleResponseDto(study)
    }

    fun createStories(imageList: List<MultipartFile>?, textList: List<String>?, uuid: String): List<StudySimpleResponseDto> {
        if (imageList == null || textList == null) {
            throw CustomException(ErrorCode.IMAGE_TEXT_REQUIRED)
        } else if (imageList.size != textList.size) {
            throw CustomException(ErrorCode.IMAGE_TEXT_NOT_MATCHING)
        }

        val user = SecurityContextHolder.getContext().authentication.principal as User

        val storyList = arrayListOf<Story>()
        val savedImageList =  arrayListOf<String>()

        for (i in imageList.indices) {
            val image: String = try {
                awsS3Service.uploadObject(imageList[i])
            } catch (e: Exception) {
                for (image in savedImageList) {
                    awsS3Service.deleteObject(image)
                }
                throw CustomException(ErrorCode.IMAGE_SAVE_ERROR)
            }
            savedImageList.add(image)
            val story = Story(
                text = textList[i],
                image = image,
                user = user,
                uuid = uuid
            )
            storyList.add(story)
        }

        return storyRepository.saveAll(storyList).map { StudySimpleResponseDto(it) }
    }

}