package com.fillingsnap.server.infra.oracle

import com.fillingsnap.server.domain.user.domain.User
import com.oracle.bmc.objectstorage.ObjectStorageClient
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest
import com.oracle.bmc.objectstorage.requests.GetNamespaceRequest
import com.oracle.bmc.objectstorage.requests.GetObjectRequest
import com.oracle.bmc.objectstorage.requests.PutObjectRequest
import com.oracle.bmc.objectstorage.transfer.UploadConfiguration
import com.oracle.bmc.objectstorage.transfer.UploadManager
import org.springframework.core.env.Environment
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.URLDecoder
import java.net.URLEncoder
import java.time.LocalDateTime

@Service
class ObjectStorageService (

    private val client: ObjectStorageClient,

    env: Environment

) {

    private val namespace: String = client.getNamespace(
        GetNamespaceRequest.builder().build()
    ).value

    private val bucketName = env.getProperty("oracle.object-storage.bucket-name")

    private val url = "https://" + namespace + ".objectstorage.ap-chuncheon-1.oci.customer-oci.com/n/" +
            namespace + "/b/" + bucketName + "/o/"

    fun getObject(image: String): InputStream {
        val request = GetObjectRequest.builder()
            .namespaceName(namespace)
            .bucketName(bucketName)
            .objectName(URLDecoder.decode(image.substring(url.length), "UTF-8"))
            .build()

        val response = client.getObject(request)

        return response.inputStream
    }

    fun uploadFile(file: MultipartFile): String {
        val user = SecurityContextHolder.getContext().authentication.principal as User

        val uploadConfiguration = UploadConfiguration.builder()
            .allowMultipartUploads(true)
            .allowParallelUploads(true)
            .build()
        val uploadManager = UploadManager(client, uploadConfiguration)

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

        return url + URLEncoder.encode(fileName, "UTF-8")
    }

    fun deleteFile(fileName: String) {
        val objectName = URLDecoder.decode(fileName.substring(url.length), "UTF-8")

        val request = DeleteObjectRequest.builder()
            .bucketName(bucketName)
            .namespaceName(namespace)
            .objectName(objectName)
            .build()

        client.deleteObject(request)
    }

}