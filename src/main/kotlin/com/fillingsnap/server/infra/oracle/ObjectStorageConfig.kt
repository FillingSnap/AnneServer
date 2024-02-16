package com.fillingsnap.server.infra.oracle

import com.oracle.bmc.ConfigFileReader
import com.oracle.bmc.Region
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider
import com.oracle.bmc.objectstorage.ObjectStorageClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

@Configuration
class ObjectStorageConfig (

    private val env: Environment

) {

    @Bean
    fun client(): ObjectStorageClient {
        val configurationFilePath = env.getProperty("oracle.object-storage.configuration-file-path")
        val profile = env.getProperty("oracle.object-storage.profile")


        val config = ConfigFileReader.parse(configurationFilePath, profile)
        val provider = ConfigFileAuthenticationDetailsProvider(config)
        val client = ObjectStorageClient.builder().build(provider)
        client.setRegion(Region.AP_CHUNCHEON_1)

        return ObjectStorageClient.builder()
            .build(provider)
    }

}