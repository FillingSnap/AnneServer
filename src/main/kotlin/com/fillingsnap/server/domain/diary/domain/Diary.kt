package com.fillingsnap.server.domain.diary.domain

import com.fillingsnap.server.domain.story.domain.Story
import com.fillingsnap.server.domain.user.domain.User
import jakarta.persistence.*

@Entity
class Diary (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    val emotion: String,

    @ElementCollection
    val imageList: List<String>,

    @ManyToOne(fetch = FetchType.LAZY)
    val user: User,

    @OneToMany(
        fetch = FetchType.LAZY,
        mappedBy = "diary",
        cascade = [CascadeType.REMOVE]
    )
    val storyList: List<Story> = ArrayList()

) {
}