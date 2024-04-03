package com.fillingsnap.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class FillingSnapServerApplication

fun main(args: Array<String>) {
    runApplication<FillingSnapServerApplication>(*args)
}
