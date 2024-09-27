package com.dangochat.dango.repository;

import com.dangochat.dango.entity.MemberEntity;
import com.dangochat.dango.entity.UserStudyContentEntity;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserStudyContentRepository extends JpaRepository<UserStudyContentEntity, Integer> {
	
	 // MemberEntity를 기반으로 사용자의 공부 기록을 조회하는 메서드
    List<UserStudyContentEntity> findByUser(MemberEntity user);
    
 // 사용자의 오늘 공부 기록을 찾는 메서드 (날짜 범위로 조회)
    List<UserStudyContentEntity> findByUserAndRecordStudyDateBetween(MemberEntity user, LocalDateTime startOfToday, LocalDateTime endOfToday);
}

