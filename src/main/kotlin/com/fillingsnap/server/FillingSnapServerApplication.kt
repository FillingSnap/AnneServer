package com.fillingsnap.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FillingSnapServerApplication

fun main(args: Array<String>) {
    runApplication<FillingSnapServerApplication>(*args)
}
