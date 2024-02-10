package com.fillingsnap.server.domain.story.web

import com.fillingsnap.server.domain.story.dto.SimpleStudyDto
import com.fillingsnap.server.domain.story.service.StoryService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/story")
class StoryController (

    private val storyService: StoryService

) {

    @GetMapping("/{id}")
    fun getStoryById(@PathVariable id: Long): SimpleStudyDto {
        return storyService.getStoryById(id)
    }

    @GetMapping("/list/today")
    fun getTodayStoryList(): List<SimpleStudyDto> {
        return storyService.getTodayStoryList()
    }

}