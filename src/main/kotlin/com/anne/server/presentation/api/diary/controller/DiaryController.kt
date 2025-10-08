package com.anne.server.presentation.api.diary.controller

import com.anne.server.application.user.dto.AuthPrincipal
import com.anne.server.application.diary.port.`in`.DiaryUseCase
import com.anne.server.domain.Diary
import com.anne.server.presentation.api.diary.dto.request.UpdateRequest
import com.anne.server.presentation.validation.ValidationSequence
import io.swagger.v3.oas.annotations.Operation
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/diary")
class DiaryController(

    private val diaryUseCase: DiaryUseCase,

) {

    @Operation(summary = "일기 전체 조회")
    @GetMapping
    fun getDiaryList(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @PageableDefault(size = 15, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable
    ): Page<Diary> =
        diaryUseCase.getDiaryList(principal.id, pageable)

    @Operation(summary = "일기 단일 조회")
    @GetMapping("/{uuid}")
    fun getDiary(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @PathVariable("uuid") uuid: String
    ): Diary =
        diaryUseCase.getDiaryByUuid(principal.id, uuid)

    @Operation(summary = "일기 수정")
    @PutMapping("/update/{uuid}")
    fun updateDiary(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @PathVariable("uuid") uuid: String,
        @RequestBody @Validated(value = [ValidationSequence::class]) request: UpdateRequest
    ): Diary =
        diaryUseCase.putDiary(principal.id, uuid, request.content)

    @Operation(summary = "일기 삭제")
    @DeleteMapping("/delete/{uuid}")
    fun deleteDiary(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @PathVariable("uuid") uuid: String
    ) =
        diaryUseCase.deleteDiary(principal.id, uuid)

}