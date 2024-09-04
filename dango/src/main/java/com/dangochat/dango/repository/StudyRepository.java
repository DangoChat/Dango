package com.dangochat.dango.repository;

import com.dangochat.dango.entity.StudyEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;  // JpaRepository를 import
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyRepository extends JpaRepository<StudyEntity, Integer> {

    // 네이티브 쿼리로 레벨이 특정한 값인 데이터 중에서 랜덤으로 limit 개수만큼 가져오기
    @Query(value = "SELECT * FROM Study_Content WHERE level = :level ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<StudyEntity> findRandomByLevel(@Param("level") String level, @Param("limit") int limit);
}
