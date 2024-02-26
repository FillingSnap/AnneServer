package com.fillingsnap.server.infra.oracle

import com.oracle.bmc.ConfigFileReader
import com.oracle.bmc.Region
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider
import com.oracle.bmc.objectstorage.ObjectStorageClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ObjectStorageConfig (

    @Value("\${oracle.object-storage.configuration-file-path}")
    private val configurationFilePath: String,

    @Value("\${oracle.object-storage.profile}")
    private val profile: String,

) {

    @Bean
    fun client(): ObjectStorageClient {
        val config = ConfigFileReader.parse(configurationFilePath, profile)
        val provider = ConfigFileAuthenticationDetailsProvider(config)
        val client = ObjectStorageClient.builder().build(provider)
        client.setRegion(Region.AP_CHUNCHEON_1)

        return ObjectStorageClient.builder()
            .build(provider)
    }

}