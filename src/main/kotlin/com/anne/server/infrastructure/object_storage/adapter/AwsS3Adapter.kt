package com.anne.server.infrastructure.object_storage.adapter

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.DeleteObjectRequest
import com.amazonaws.services.s3.model.DeleteObjectsRequest
import com.amazonaws.services.s3.model.ObjectMetadata
import com.anne.server.application.story.port.out.ObjectStorage
import com.anne.server.common.exception.CustomException
import com.anne.server.common.exception.ErrorCode
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.net.URLDecoder
import java.time.LocalDateTime

@Service
class AwsS3Adapter (

    private val s3: AmazonS3,

    @Value("\${cloud.aws.s3.bucket}")
    private val bucket: String

): ObjectStorage {

    private val url = "https://$bucket.s3.ap-northeast-2.amazonaws.com/"

    private val extList = listOf("png", "jpg", "jpeg")

    override fun uploadObject(userId: Long, file: MultipartFile): String {
        val originalFileName = file.originalFilename!!
        val ext = originalFileName.substring(originalFileName.lastIndexOf(".") + 1)

        if (ext !in extList) {
            throw CustomException(ErrorCode.WRONG_IMAGE_EXTENSION)
        }

        val fileName = userId.toString() + "/" + LocalDateTime.now().toString() + "." + ext
        val metadata = ObjectMetadata()
        metadata.contentLength = file.size
        metadata.contentType = file.contentType

        try {
            s3.putObject(bucket, fileName, file.inputStream, metadata)

            return s3.getUrl(bucket, fileName).toString()
        } catch (_: Exception) {
            throw CustomException(ErrorCode.OBJECT_STORAGE_ERROR)
        }
    }

    override fun deleteObject(image: String) {
        try {
            s3.deleteObject(DeleteObjectRequest(bucket, URLDecoder.decode(image.substring(url.length), "UTF-8")))
        } catch (_: Exception) {
            throw CustomException(ErrorCode.OBJECT_STORAGE_ERROR)
        }
    }

}