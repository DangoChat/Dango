package com.dangochat.dango.repository;

import com.dangochat.dango.entity.StudyEntity;
import org.springframework.data.jpa.repository.JpaRepository;  // JpaRepository를 import
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyRepository extends JpaRepository<StudyEntity, Integer> {

    @Query(value = "SELECT * FROM Study_Content WHERE level = :level ORDER BY RAND() LIMIT :limit", nativeQuery = true)

    //findRandomByLevel 메서드를 호출 하는 곳에 level 이랑, limit 전달
    List<StudyEntity> findRandomByLevel(@Param("level") String level, @Param("limit") int limit);

    // 특정 유저의 오답 노트(해결 되지 않은)에서 studyContentId에 해당하는 StudyEntity를 가져오는 메서드
    @Query("SELECT s FROM StudyEntity s JOIN UserMistakesEntity m ON s.studyContentId = m.studyContentId WHERE m.userId = :userId AND m.mistakeResolved = false ORDER BY RAND() LIMIT :limit")
    List<StudyEntity> findMistakesByUserId(@Param("userId") int userId, @Param("limit") int limit);
}