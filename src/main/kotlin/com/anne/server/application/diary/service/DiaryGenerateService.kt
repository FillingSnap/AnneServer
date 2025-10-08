package com.anne.server.application.diary.service

import com.anne.server.application.diary.dto.ImageTextDto
import com.anne.server.application.diary.port.`in`.DiaryGenerateUseCase
import com.anne.server.application.diary.port.out.DiaryRepository
import com.anne.server.application.diary.port.out.DiaryStreamGenerator
import com.anne.server.application.story.port.out.StoryRepository
import com.anne.server.domain.Diary
import com.anne.server.common.exception.CustomException
import com.anne.server.common.exception.ErrorCode
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class DiaryGenerateService(

    private val diaryRepository: DiaryRepository,

    private val storyRepository: StoryRepository,

    private val diaryStreamGenerator: DiaryStreamGenerator

): DiaryGenerateUseCase {

    override fun streamDiary(
        userId: Long,
        uuid: String
    ): Flux<String> {
        if (diaryRepository.existsByUuid(uuid)) {
            return Flux.error(CustomException(ErrorCode.ALREADY_EXIST_UUID))
        }
        if (!storyRepository.existsByUuid(uuid)) {
            return Flux.error(CustomException(ErrorCode.STORY_NOT_FOUND))
        }

        return diaryStreamGenerator.diaryStream(
            storyRepository.findAllByUuid(uuid)
                .map { ImageTextDto(it.image, it.text) }
        ).takeUntil { it.error }
            .flatMap {
                if (it.error) Flux.error(RuntimeException(it.message))
                else Flux.just(it.message)
            }
    }

    @Transactional
    override fun generateDiary(
        userId: Long,
        uuid: String,
        content: String
    ): Diary =
        diaryRepository.save(
            Diary(
                uuid = uuid,
                userId = userId,
                content = content
            )
        ).also { diary ->
            storyRepository.findAllByUuid(uuid)
                .map { story ->
                    story.diaryId = diary.id
                    return@map story
                }
                .also {
                    storyRepository.saveAll(it)
                }
        }

}