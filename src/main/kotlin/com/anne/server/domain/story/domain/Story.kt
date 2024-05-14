package com.anne.server.domain.story.domain

import com.anne.server.domain.diary.domain.Diary
import com.anne.server.domain.user.domain.User
import com.anne.server.global.model.BaseTimeEntity
import jakarta.persistence.*

@Entity
class Story (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(columnDefinition = "LONGTEXT")
    val text: String,

    val image: String,

    val uuid: String,

    @ManyToOne(fetch = FetchType.LAZY)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    var diary: Diary? = null

): BaseTimeEntity() {
}