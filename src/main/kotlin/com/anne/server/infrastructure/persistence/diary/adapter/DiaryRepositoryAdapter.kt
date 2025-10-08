package com.anne.server.infrastructure.persistence.diary.adapter

import com.anne.server.application.diary.port.out.DiaryRepository
import com.anne.server.domain.Diary
import com.anne.server.infrastructure.persistence.diary.mapper.DiaryMapper
import com.anne.server.infrastructure.persistence.diary.repository.DiaryJpaRepository
import com.anne.server.infrastructure.persistence.user.entity.UserJpaEntity
import jakarta.persistence.EntityManager
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class DiaryRepositoryAdapter(

    private val diaryJpaRepository: DiaryJpaRepository,

    private val em: EntityManager

): DiaryRepository {

    private val userRef: (Long) -> UserJpaEntity = {
        em.getReference(UserJpaEntity::class.java, it)
    }

    override fun existsByUuid(uuid: String): Boolean =
        diaryJpaRepository.existsByUuid(uuid)

    override fun findByUuid(uuid: String): Diary? =
        diaryJpaRepository.findByUuid(uuid)
            ?.let { DiaryMapper.toDomain(it) }

    override fun findAllByUserId(
        userId: Long,
        pageable: Pageable
    ): Page<Diary> =
        diaryJpaRepository.findAllByUser(userRef(userId), pageable)
            .map { DiaryMapper.toDomain(it) }


    override fun save(diary: Diary): Diary =
        DiaryMapper.toDomain(
            diaryJpaRepository.save(
                DiaryMapper.toEntity(diary, userRef)
            )
        )

    override fun deleteById(id: Long) =
        diaryJpaRepository.deleteById(id)

}