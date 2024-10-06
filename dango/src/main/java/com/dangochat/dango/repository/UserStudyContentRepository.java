package com.dangochat.dango.repository;

import com.dangochat.dango.entity.MemberEntity;
import com.dangochat.dango.entity.UserStudyContentEntity;


import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserStudyContentRepository extends JpaRepository<UserStudyContentEntity, Integer> {
	
	 // MemberEntity를 기반으로 사용자의 공부 기록을 조회하는 메서드
    List<UserStudyContentEntity> findByUser(MemberEntity user);
    
 // 사용자의 오늘 공부 기록을 찾는 메서드 (날짜 범위로 조회)
    List<UserStudyContentEntity> findByUserAndRecordStudyDateBetween(MemberEntity user, LocalDateTime startOfToday, LocalDateTime endOfToday);


 // user_id에 따라 학습한 내용을 가져오는 쿼리
    @Query("SELECT usc FROM UserStudyContentEntity usc JOIN usc.studyContent sc WHERE usc.user.id = :userId")
    List<UserStudyContentEntity> findStudyContentByUserId(@Param("userId") int userId);

    // 사용자가 공부한 날짜 기록
    @Query("SELECT DISTINCT DATE(usc.recordStudyDate) FROM UserStudyContentEntity usc WHERE usc.user.id = :userId")
    List<String> findStudyDatesByUserId(@Param("userId") int userId);

    @Query("SELECT usc FROM UserStudyContentEntity usc WHERE usc.user.id = :userId AND DATE(usc.recordStudyDate) = :date")
    List<UserStudyContentEntity> findStudyContentByUserIdAndDate(@Param("userId") int userId, @Param("date") String date);

    @Query("SELECT usc FROM UserStudyContentEntity usc WHERE usc.user.id = :userId AND DATE(usc.recordStudyDate) = :date")
    List<UserStudyContentEntity> findStudyContentByUserIdAndDate(@Param("userId") int userId, @Param("date") Date date);

}

