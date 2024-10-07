package com.dangochat.dango.service;

import com.dangochat.dango.entity.UserRankingEntity;
import com.dangochat.dango.repository.UserRankingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
@Slf4j
@RequiredArgsConstructor
@Service
public class UserRankingService {

    private final UserRankingRepository userRankingRepository;

    @Transactional
    public void updateUserRanking(int userId, int gameScore) {
        try {
            // 기존 랭킹 정보 조회
            UserRankingEntity existingRanking = userRankingRepository.findByUserIdAndRankingCategory(userId, "끝말잇기");

            if (existingRanking != null) {
                // 기존 포인트에 더하지 않고 새로운 점수로 덮어씀
                existingRanking.setRankingPoint(gameScore);
                existingRanking.setRankingDate(LocalDateTime.now());  // 날짜 업데이트
                userRankingRepository.save(existingRanking);
            } else {
                // 새로운 랭킹 생성
                UserRankingEntity newRanking = new UserRankingEntity();
                newRanking.setUserId(userId);
                newRanking.setRankingCategory("끝말잇기");
                newRanking.setRankingPoint(gameScore);
                newRanking.setRankingDate(LocalDateTime.now());  // 현재 날짜와 시간
                userRankingRepository.save(newRanking);
            }
        } catch (Exception e) {
            log.error("랭킹 업데이트 중 오류 발생: ", e);  // 오류 발생 시 로그 출력
        }
    }
}
