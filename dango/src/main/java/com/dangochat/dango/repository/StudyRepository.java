package com.dangochat.dango.repository;

import com.dangochat.dango.entity.StudyEntity;
import org.springframework.data.jpa.repository.JpaRepository;  // JpaRepository를 import
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyRepository extends JpaRepository<StudyEntity, Integer> {

    @Query(value = "SELECT sc.* FROM Study_Content sc " +
            "JOIN User_Study_Content usc ON sc.study_content_id = usc.study_content_id " +
            "JOIN Users u ON usc.user_id = u.user_id " +
            "WHERE sc.level = :level AND u.user_id = :userId " +
            "ORDER BY RAND() LIMIT 20", nativeQuery = true)
    List<StudyEntity> findRandomByLevelAndUserId(@Param("level") String level, @Param("userId") String userId);
}