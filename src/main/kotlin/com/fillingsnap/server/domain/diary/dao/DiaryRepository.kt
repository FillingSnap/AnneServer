package com.fillingsnap.server.domain.diary.dao

import com.fillingsnap.server.domain.diary.domain.Diary
import org.springframework.data.jpa.repository.JpaRepository

interface DiaryRepository: JpaRepository<Diary, Long> {
}