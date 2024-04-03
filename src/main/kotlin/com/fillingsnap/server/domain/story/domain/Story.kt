package com.fillingsnap.server.domain.story.domain

import com.fillingsnap.server.domain.diary.domain.Diary
import com.fillingsnap.server.domain.user.domain.User
import com.fillingsnap.server.global.model.BaseTimeEntity
import jakarta.persistence.*
import java.time.LocalDateTime

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