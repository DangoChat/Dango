package com.dangochat.dango.repository;

import com.dangochat.dango.entity.UserMistakesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMistakesRepository extends JpaRepository<UserMistakesEntity, Integer> {

    // 특정 유저와 학습 콘텐츠에 대한 오답 기록 조회
    UserMistakesEntity findByUserIdAndStudyContentId(String userId, int studyContentId);

    // 특정 유저의 학습 콘텐츠에 대한 오답 기록 삭제
    void deleteByUserIdAndStudyContentId(String userId, int studyContentId);
}

