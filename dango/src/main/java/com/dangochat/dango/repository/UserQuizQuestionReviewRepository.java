package com.dangochat.dango.repository;

import com.dangochat.dango.entity.QuizType;
import com.dangochat.dango.entity.UserQuizQuestionReviewEntity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserQuizQuestionReviewRepository extends JpaRepository<UserQuizQuestionReviewEntity, Integer> {
    
	@Query("SELECT q FROM UserQuizQuestionReviewEntity q WHERE q.quizType = :quizType AND q.user.id = :userId")
	List<UserQuizQuestionReviewEntity> findByQuizTypeAndUserId(@Param("quizType") QuizType quizType, @Param("userId") int userId);
	
	 List<UserQuizQuestionReviewEntity> findByUserQuizQuestionIdIn(List<Integer> ids);
}
