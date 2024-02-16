package com.fillingsnap.server.infra.oracle

import com.fillingsnap.server.domain.user.domain.User
import com.oracle.bmc.objectstorage.ObjectStorageClient
import com.oracle.bmc.objectstorage.requests.GetNamespaceRequest
import com.oracle.bmc.objectstorage.requests.PutObjectRequest
import com.oracle.bmc.objectstorage.transfer.UploadConfiguration
import com.oracle.bmc.objectstorage.transfer.UploadManager
import org.springframework.core.env.Environment
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.net.URLEncoder
import java.time.LocalDateTime

@Service
class ObjectStorageService (

    private val client: ObjectStorageClient,

    private val env: Environment

) {

    fun uploadFile(file: MultipartFile): String {
        val user = SecurityContextHolder.getContext().authentication.principal as User

        val namespace = client.getNamespace(
            GetNamespaceRequest.builder().build()
        ).value

        val uploadConfiguration = UploadConfiguration.builder()
            .allowMultipartUploads(true)
            .allowParallelUploads(true)
            .build()
        val uploadManager = UploadManager(client, uploadConfiguration)

        val bucketName = env.getProperty("oracle.object-storage.bucket-name")
        val originalFileName = file.originalFilename!!
        val ext = originalFileName.substring(originalFileName.lastIndexOf(".") + 1)
        val fileName = user.id!!.toString() + "/" + LocalDateTime.now().toString() + "." + ext

        val request = PutObjectRequest.builder()
            .bucketName(bucketName)
            .namespaceName(namespace)
            .objectName(fileName)
            .contentType("image/jpg")
            .contentType(null)
            .contentLanguage(null)
            .contentEncoding(null)
            .opcMeta(null)
            .build()

        val inputStream = ByteArrayInputStream(file.bytes)

        val uploadDetails = UploadManager.UploadRequest
            .builder(
                inputStream,
                inputStream.available().toLong()
            )
            .allowOverwrite(true)
            .build(request)
        uploadManager.upload(uploadDetails)

        return "https://" + namespace + ".objectstorage.ap-chuncheon-1.oci.customer-oci.com/n/" +
                namespace + "/b/" + bucketName + "/o/" + URLEncoder.encode(fileName, "UTF-8")
    }

}