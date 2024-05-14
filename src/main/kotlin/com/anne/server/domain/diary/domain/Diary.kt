package com.anne.server.domain.diary.domain

import com.anne.server.domain.story.domain.Story
import com.anne.server.domain.user.domain.User
import com.anne.server.global.model.BaseTimeEntity
import jakarta.persistence.*

@Entity
class Diary (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val emotion: String,

    @Column(columnDefinition = "LONGTEXT")
    val content: String,

    val uuid: String,

    @ManyToOne(fetch = FetchType.LAZY)
    val user: User,

    @OneToMany(
        fetch = FetchType.LAZY,
        mappedBy = "diary",
        cascade = [CascadeType.REMOVE]
    )
    val storyList: List<Story> = ArrayList()

): BaseTimeEntity() {
}