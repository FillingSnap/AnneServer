package com.fillingsnap.server.domain.user.domain

import com.fillingsnap.server.domain.diary.domain.Diary
import com.fillingsnap.server.domain.story.domain.Story
import com.fillingsnap.server.global.model.BaseTimeEntity
import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

@Entity
class User (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    val name: String,

    val uid: String,

    val provider: String,

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

): BaseTimeEntity(), UserDetails {

    override fun getAuthorities(): MutableCollection<out GrantedAuthority>? {
        return null
    }

    override fun getPassword(): String? {
        return null
    }

    override fun getUsername(): String {
        return id.toString()
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }

}