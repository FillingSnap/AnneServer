package com.anne.server.application.story.port.out

import org.springframework.web.multipart.MultipartFile

interface ObjectStorage {

    fun uploadObject(userId: Long, file: MultipartFile): String

    fun deleteObject(image: String)

}