package com.anne.server.domain

data class User(

    var id: Long? = null,

    val name: String,

    val uid: String,

    val provider: String,

    var styleList: List<String> = emptyList()

)