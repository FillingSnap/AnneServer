package com.anne.server.infrastructure.persistence.user.entity

import com.anne.server.infrastructure.persistence.common.BaseTimeEntity
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "user")
class UserJpaEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    val name: String,

    val uid: String,

    val provider: String,

    @ElementCollection(fetch = FetchType.EAGER)
    var styleList: List<String> = ArrayList(),

): BaseTimeEntity()