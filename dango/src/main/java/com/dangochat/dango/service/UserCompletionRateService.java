package com.dangochat.dango.service;

import java.util.List;

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
    public List<UserCompletionRateEntity> getCompletionRatesByUserId(int userId) {
        log.info("Fetching completion rates for userId: {}", userId);
        return userCompletionRateRepository.findByUser_UserId(userId);
    }
	
	
}
