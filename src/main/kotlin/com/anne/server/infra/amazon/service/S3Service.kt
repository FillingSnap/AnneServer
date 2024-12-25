package com.anne.server.infra.amazon.service

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.DeleteObjectRequest
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.ObjectMetadata
import com.anne.server.domain.user.domain.User
import com.anne.server.domain.user.dto.UserDto
import com.anne.server.global.exception.CustomException
import com.anne.server.global.exception.ErrorCode
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream
import java.net.URLDecoder
import java.time.LocalDateTime

@Service
class S3Service (

    private val s3: AmazonS3,

    @Value("\${cloud.aws.s3.bucket}")
    private val bucket: String

) {

    private val url = "https://$bucket.s3.ap-northeast-2.amazonaws.com/"

    fun getObject(image: String): InputStream {
        try {
            return s3.getObject(GetObjectRequest(bucket, URLDecoder.decode(image.substring(url.length), "UTF-8")))
                .objectContent
                .delegateStream
        } catch (e: Exception) {
            throw CustomException(ErrorCode.AWS_S3_ERROR)
        }
    }

    fun uploadObject(file: MultipartFile): String {
        val userDto = SecurityContextHolder.getContext().authentication.principal as UserDto

        val originalFileName = file.originalFilename!!
        val ext = originalFileName.substring(originalFileName.lastIndexOf(".") + 1)
        val fileName = UserDto.toEntity(userDto).id!!.toString() + "/" + LocalDateTime.now().toString() + "." + ext
        val metadata = ObjectMetadata()
        metadata.contentLength = file.size
        metadata.contentType = file.contentType

        try {
            s3.putObject(bucket, fileName, file.inputStream, metadata)

            return s3.getUrl(bucket, fileName).toString()
        } catch (e: Exception) {
            throw CustomException(ErrorCode.AWS_S3_ERROR)
        }
    }

    fun deleteObject(image: String) {
        try {
            s3.deleteObject(DeleteObjectRequest(bucket, URLDecoder.decode(image.substring(url.length), "UTF-8")))
        } catch (e: Exception) {
            throw CustomException(ErrorCode.AWS_S3_ERROR)
        }
    }

}