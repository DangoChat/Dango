package com.dangochat.dango.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dangochat.dango.entity.UserCompletionRateEntity;

@Repository
public interface UserCompletionRateRepository extends JpaRepository<UserCompletionRateEntity, Integer> {
    
	// 특정 사용자의 기록 조회
	 List<UserCompletionRateEntity> findByUser_UserId(int userId);
	 
	 @Modifying
	 @Query("UPDATE UserCompletionRateEntity ucr SET ucr.weeklyPoints = ucr.weeklyPoints + :points, ucr.totalPoints = ucr.totalPoints + :points WHERE ucr.user.userId = :userId")
	 void updatePoints(@Param("userId") int userId, @Param("points") int points);
	
}

