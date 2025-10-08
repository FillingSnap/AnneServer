package com.anne.server.infrastructure.persistence.story.entity

import com.anne.server.infrastructure.persistence.common.BaseTimeEntity
import com.anne.server.infrastructure.persistence.diary.entity.DiaryJpaEntity
import com.anne.server.infrastructure.persistence.user.entity.UserJpaEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "story")
class StoryJpaEntity (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(columnDefinition = "LONGTEXT")
    val text: String,

    val image: String,

    val uuid: String,

    @ManyToOne(fetch = FetchType.LAZY)
    val user: UserJpaEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    var diary: DiaryJpaEntity? = null

): BaseTimeEntity()