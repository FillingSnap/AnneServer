package com.anne.server.domain.diary.service

import com.anne.server.domain.diary.dao.DiaryRepository
import com.anne.server.domain.diary.domain.Diary
import com.anne.server.domain.diary.dto.response.DiaryWithStoryResponseDto
import com.anne.server.domain.diary.dto.request.DiaryUpdateRequestDto
import com.anne.server.domain.story.dao.StoryRepository
import com.anne.server.domain.user.domain.User
import com.anne.server.global.exception.CustomException
import com.anne.server.global.exception.ErrorCode
import com.anne.server.infra.openai.OpenAiService
import com.anne.server.infra.openai.dto.SseResponseDto
import com.anne.server.infra.openai.dto.SseStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux

@Service
class DiaryService(

    private val diaryRepository: DiaryRepository,

    private val storyRepository: StoryRepository,

    private val openAiService: OpenAiService

) {

    // 수정 필요
    @Transactional
    fun generateDiary(delay: Long, uuid: String): Flux<SseResponseDto> {
        if (diaryRepository.existsDiaryByUuid(uuid)) {
            return Flux.just(
                SseResponseDto(
                    status = SseStatus.ERROR,
                    content = ErrorCode.ALREADY_EXIST_UUID.message
                )
            )
        }

        if (!storyRepository.existsStoryByUuid(uuid)) {
            return Flux.just(
                SseResponseDto(
                    status = SseStatus.ERROR,
                    content = ErrorCode.STORY_NOT_FOUND.message
                )
            )
        }

        val user = SecurityContextHolder.getContext().authentication.principal as User

        diaryRepository.save(
            Diary(
                emotion = "null",
                content = "null",
                user = user,
                uuid = uuid
            )
        )

        return openAiService.openAi(uuid, delay)
    }

    @Transactional(readOnly = true)
    fun getDiaryList(pageable: Pageable): Page<DiaryWithStoryResponseDto> {
        val user = SecurityContextHolder.getContext().authentication.principal as User

        val page = diaryRepository.findAllByUser(user, pageable).map {
            DiaryWithStoryResponseDto(it)
        }

        if (page.totalPages != 0 && page.totalPages <= page.number) {
            throw CustomException(ErrorCode.WRONG_PAGE)
        }

        return page
    }

    @Transactional(readOnly = true)
    fun getDiaryByUuid(uuid: String): DiaryWithStoryResponseDto {
        val diary = diaryRepository.findByUuid(uuid)
            ?: throw CustomException(ErrorCode.DIARY_NOT_FOUND)

        val user = SecurityContextHolder.getContext().authentication.principal as User

        // 해당 일기의 소유자가 아닌 경우
        if (user.id!! != diary.user.id!!) {
            throw CustomException(ErrorCode.NOT_YOUR_DIARY)
        }

        return DiaryWithStoryResponseDto(diary)
    }

    @Transactional
    fun updateDiary(uuid: String, request: DiaryUpdateRequestDto): DiaryWithStoryResponseDto {
        val user = SecurityContextHolder.getContext().authentication.principal as User

        val diary = diaryRepository.findByUuid(uuid)
            ?: throw CustomException(ErrorCode.DIARY_NOT_FOUND)

        // 해당 일기의 소유자가 아닌 경우
        if (user.id!! != diary.user.id!!) {
            throw CustomException(ErrorCode.NOT_YOUR_DIARY)
        }

        diary.content = request.content

        diaryRepository.save(diary)

        return DiaryWithStoryResponseDto(diary)
    }

}