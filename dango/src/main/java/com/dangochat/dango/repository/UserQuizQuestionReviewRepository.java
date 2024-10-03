package com.dangochat.dango.repository;

import com.dangochat.dango.entity.UserQuizQuestionReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserQuizQuestionReviewRepository extends JpaRepository<UserQuizQuestionReviewEntity, Integer> {
    // 필요한 경우, 사용자 정의 쿼리를 여기에 추가할 수 있음
	
}
