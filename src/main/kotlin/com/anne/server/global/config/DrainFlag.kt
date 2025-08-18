package com.anne.server.global.config

import org.springframework.context.annotation.Configuration
import java.util.concurrent.atomic.AtomicBoolean

@Configuration
class DrainFlag {

    val acceptNew = AtomicBoolean(true)

}