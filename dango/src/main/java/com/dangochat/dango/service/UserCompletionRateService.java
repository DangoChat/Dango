package com.dangochat.dango.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.dangochat.dango.entity.UserCompletionRateEntity;
import com.dangochat.dango.repository.UserCompletionRateRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCompletionRateService {

	private final UserCompletionRateRepository userCompletionRateRepository;
	 // 특정 사용자의 completion rate 기록 조회
    public Optional<UserCompletionRateEntity> getCompletionRatesByUserId(int userId) {
        log.info("Fetching completion rates for userId: {}", userId);
        return userCompletionRateRepository.findByUser_UserId(userId);
    }
    
    
    
    public int getUserRank(int userId) {
        List<UserCompletionRateEntity> rankings = userCompletionRateRepository.findAllByOrderByWeeklyPointsDesc();

        // 현재 로그인한 사용자의 순위 찾기
        for (int i = 0; i < rankings.size(); i++) {
            if (rankings.get(i).getUser().getUserId() == userId) { // 수정된 부분
                return i + 1; // 순위는 1부터 시작
            }
        }
        return -1; // 사용자가 순위에 없을 경우 -1 반환
    }
	
	
}
