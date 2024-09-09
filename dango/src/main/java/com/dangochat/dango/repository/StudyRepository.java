package com.dangochat.dango.repository;

import com.dangochat.dango.entity.StudyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyRepository extends JpaRepository<StudyEntity, Integer> {

    // Native Query로 레벨을 기준으로 학습 콘텐츠를 랜덤하게 가져오고, LIMIT을 적용
    @Query(value = "SELECT * FROM Study_Content WHERE level = :level AND study_content_type = :type ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<StudyEntity> findRandomByLevelAndType(@Param("level") String level, @Param("type") String type, @Param("limit") int limit);

    // Native Query로 특정 유저의 오답 노트(해결되지 않은)에서 studyContentId에 해당하는 StudyEntity를 랜덤하게 가져오고, LIMIT 적용
    @Query(value = "SELECT s.* FROM Study_Content s JOIN User_Mistakes m ON s.study_content_id = m.study_content_id WHERE m.user_id = :userId AND m.mistake_resolved = false AND s.study_content_type = :type ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<StudyEntity> findMistakesByUserIdAndType(@Param("userId") int userId, @Param("type") String type, @Param("limit") int limit);
}
