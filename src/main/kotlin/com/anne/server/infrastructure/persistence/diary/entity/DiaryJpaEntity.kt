package com.anne.server.infrastructure.persistence.diary.entity

import com.anne.server.infrastructure.persistence.common.BaseTimeEntity
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
@Table(name = "diary")
class DiaryJpaEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val emotion: String?,

    @Column(columnDefinition = "LONGTEXT")
    var content: String,

    val uuid: String,

    @ManyToOne(fetch = FetchType.LAZY)
    val user: UserJpaEntity,

): BaseTimeEntity()