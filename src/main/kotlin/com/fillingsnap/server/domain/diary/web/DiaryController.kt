package com.fillingsnap.server.domain.diary.web

import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/diary")
class DiaryController (

    private val sendingOperations: SimpMessageSendingOperations

) {

    @GetMapping("test")
    fun test(id: Long) {
        sendingOperations.convertAndSend("/queue/channel/$id", "hello")
    }

}