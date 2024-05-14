package com.anne.server.infra.amazon

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.DeleteObjectRequest
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.ObjectMetadata
import com.anne.server.domain.user.domain.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream
import java.net.URLDecoder
import java.time.LocalDateTime

@Service
class AwsS3Service (

    private val s3: AmazonS3,

    @Value("\${cloud.aws.s3.bucket}")
    private val bucket: String

) {

    private val url = "https://$bucket.s3.ap-northeast-2.amazonaws.com/"

    fun getObject(image: String): InputStream {
        return s3.getObject(GetObjectRequest(bucket, URLDecoder.decode(image.substring(url.length), "UTF-8")))
            .objectContent
            .delegateStream
    }

    fun uploadObject(file: MultipartFile): String {
        val user = SecurityContextHolder.getContext().authentication.principal as User

        val originalFileName = file.originalFilename!!
        val ext = originalFileName.substring(originalFileName.lastIndexOf(".") + 1)
        val fileName = user.id!!.toString() + "/" + LocalDateTime.now().toString() + "." + ext
        val metadata = ObjectMetadata()
        metadata.contentLength = file.size
        metadata.contentType = file.contentType
        s3.putObject(bucket, fileName, file.inputStream, metadata)

        return s3.getUrl(bucket, fileName).toString()
    }

    fun deleteObject(image: String) {
        s3.deleteObject(DeleteObjectRequest(bucket, URLDecoder.decode(image.substring(url.length), "UTF-8")))
    }

}