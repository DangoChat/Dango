package com.dangochat.dango.service;

import com.dangochat.dango.dto.UserQuizQuestionReviewDTO;
import com.dangochat.dango.entity.MemberEntity;
import com.dangochat.dango.entity.StudyEntity;
import com.dangochat.dango.entity.UserQuizQuestionReviewEntity;
import com.dangochat.dango.repository.MemberRepository;
import com.dangochat.dango.repository.StudyRepository;
import com.dangochat.dango.repository.UserQuizQuestionReviewRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class userQuizQuestionReviewService {

	 private final UserQuizQuestionReviewRepository userQuizQuestionReviewRepository;
	    private final StudyRepository studyRepository;
	    private final MemberRepository memberRepository;

	    // 퀴즈 데이터를 저장하는 메서드
	    public void saveUserQuizQuestion(UserQuizQuestionReviewDTO reviewDTO) {
	        // 새로운 엔티티 생성
	        UserQuizQuestionReviewEntity review = new UserQuizQuestionReviewEntity();

	        
	        // MemberEntity 가져오기 (user_id를 통해 조회)
	        MemberEntity user = memberRepository.findById(reviewDTO.getUserId())
	                .orElseThrow(() -> new IllegalArgumentException("User not found"));

	        // 엔티티에 값 설정
	        
	        review.setUser(user);  // 연관된 사용자 설정
	        review.setQuizType(reviewDTO.getQuizType());  // 퀴즈 타입 설정
	        review.setQuizContent(reviewDTO.getQuizContent());  // 퀴즈 내용 설정
	        review.setQuizStatus(reviewDTO.isQuizStatus());  // 퀴즈 상태 설정

	        // 엔티티 저장
	        userQuizQuestionReviewRepository.save(review);  // 엔티티를 데이터베이스에 저장
	    }

	    public List<UserQuizQuestionReviewEntity> findQuizContentByIds(List<Integer> ids) {
	        return userQuizQuestionReviewRepository.findByUserQuizQuestionIdIn(ids);
	    }
}
