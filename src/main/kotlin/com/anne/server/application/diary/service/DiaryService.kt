package com.anne.server.application.diary.service

import com.anne.server.application.diary.port.`in`.DiaryUseCase
import com.anne.server.application.diary.port.out.DiaryRepository
import com.anne.server.application.story.port.out.ObjectStorage
import com.anne.server.application.story.port.out.StoryRepository
import com.anne.server.domain.Diary
import com.anne.server.common.exception.ErrorCode
import com.anne.server.common.exception.CustomException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DiaryService(

    private val diaryRepository: DiaryRepository,

    private val storyRepository: StoryRepository,

    private val objectStorage: ObjectStorage

): DiaryUseCase {

    @Transactional(readOnly = true)
    override fun getDiaryByUuid(userId: Long, uuid: String): Diary =
        diaryRepository.findByUuid(uuid)
            ?.also {
                if (userId != it.userId)
                    throw CustomException(ErrorCode.NOT_YOUR_DIARY)
            }
            ?: throw CustomException(ErrorCode.DIARY_NOT_FOUND)


    @Transactional(readOnly = true)
    override fun getDiaryList(
        userId: Long,
        pageable: Pageable
    ): Page<Diary> =
        diaryRepository.findAllByUserId(userId, pageable)
            .also {
                if (it.totalPages != 0 && it.totalPages <= it.number)
                    throw CustomException(ErrorCode.WRONG_PAGE)
            }

    @Transactional
    override fun putDiary(
        userId: Long,
        uuid: String,
        content: String
    ): Diary =
        diaryRepository.findByUuid(uuid)
            ?.let {
                if (it.userId != userId)
                    throw CustomException(ErrorCode.NOT_YOUR_DIARY)

                it.content = content
                diaryRepository.save(it)
            }
            ?: throw CustomException(ErrorCode.DIARY_NOT_FOUND)

    @Transactional
    override fun deleteDiary(userId: Long, uuid: String) =
        diaryRepository.findByUuid(uuid)
            ?.let {
                if (userId != it.userId)
                    throw CustomException(ErrorCode.NOT_YOUR_DIARY)

                val imageList = storyRepository.findAllByUuid(uuid)
                    .map { story -> story.image }

                diaryRepository.deleteById(it.id!!)

                imageList.forEach { image ->
                    objectStorage.deleteObject(image)
                }
            }
            ?: throw CustomException(ErrorCode.DIARY_NOT_FOUND)

}