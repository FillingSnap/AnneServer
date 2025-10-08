package com.anne.server.application.user.port.out

import java.time.Duration

interface InMemoryDbRepository {

    fun setValue(key: String, data: String, duration: Duration): String

    fun getValue(key: String): String?

}