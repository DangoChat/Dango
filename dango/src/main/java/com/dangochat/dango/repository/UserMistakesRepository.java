package com.dangochat.dango.repository;

import com.dangochat.dango.entity.MemberEntity;
import com.dangochat.dango.entity.StudyEntity;
import com.dangochat.dango.entity.UserMistakesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMistakesRepository extends JpaRepository<UserMistakesEntity, Integer> {

    // 특정 유저와 학습 콘텐츠에 대한 오답 기록 조회
    UserMistakesEntity findByUser_UserIdAndStudyContent_StudyContentId(int userId, int studyContentId);

    // 특정 유저의 학습 콘텐츠에 대한 오답 기록 삭제
    void deleteByUser_UserIdAndStudyContent_StudyContentId(int userId, int studyContentId);
}
