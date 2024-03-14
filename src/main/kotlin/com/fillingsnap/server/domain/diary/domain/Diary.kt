package com.fillingsnap.server.domain.diary.domain

import com.fillingsnap.server.domain.story.domain.Story
import com.fillingsnap.server.domain.user.domain.User
import com.fillingsnap.server.global.model.BaseTimeEntity
import jakarta.persistence.*

@Entity
class Diary (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val emotion: String,

    @Column(columnDefinition = "LONGTEXT")
    val content: String,

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