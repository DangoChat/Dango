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
}