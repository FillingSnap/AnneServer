package com.anne.server.application.user.port.`in`

interface UserStyleUseCase {

    fun getStyleList(id: Long): List<String>

    fun updateStyleList(id: Long, styleList: List<String>): List<String>

}