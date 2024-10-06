package com.dangochat.dango.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dangochat.dango.entity.UserCompletionRateEntity;

import jakarta.transaction.Transactional;

@Repository
public interface UserCompletionRateRepository extends JpaRepository<UserCompletionRateEntity, Integer> {
    
	 
	 @Modifying
	 @Query("UPDATE UserCompletionRateEntity ucr SET ucr.weeklyPoints = ucr.weeklyPoints + :points, ucr.totalPoints = ucr.totalPoints + :points WHERE ucr.user.userId = :userId")
	 void updatePoints(@Param("userId") int userId, @Param("points") int points);
	 
	 @Modifying
	 @Transactional
	 @Query("UPDATE UserCompletionRateEntity ucr SET ucr.totalPoints = 0, ucr.weeklyPoints = 0 WHERE ucr.user.userId = :userId")
	 void resetPointsByUserId(@Param("userId") Integer userId);
	
	// 유저 ID로 completion_rate 레코드 조회
	    Optional<UserCompletionRateEntity> findByUser_UserId(int userId);
}

