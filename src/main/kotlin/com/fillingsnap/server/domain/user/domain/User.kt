package com.fillingsnap.server.domain.user.domain

import com.fillingsnap.server.domain.diary.domain.Diary
import com.fillingsnap.server.domain.story.domain.Story
import com.fillingsnap.server.global.model.BaseTimeEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany

@Entity
class User (

    @Id
    var id: Long? = null,

    val name: String,

    @OneToMany(
        fetch = FetchType.LAZY,
        mappedBy = "user",
        cascade = [CascadeType.REMOVE]
    )
    val storyList: List<Story> = ArrayList(),

    @OneToMany(
        fetch = FetchType.LAZY,
        mappedBy = "user",
        cascade = [CascadeType.REMOVE]
    )
    val diaryList: List<Diary> = ArrayList()

): BaseTimeEntity() {
}