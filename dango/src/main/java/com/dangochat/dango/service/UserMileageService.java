package com.dangochat.dango.service;

import com.dangochat.dango.entity.MemberEntity;
import com.dangochat.dango.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserMileageService {

    private final MemberRepository memberRepository;

    // 마일리지 증가 로직
    public void addMileage(int userId, int mileage) {
        // userId로 회원 정보 조회
        MemberEntity user = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + userId));

        // 마일리지 업데이트
        user.setUserMileage(user.getUserMileage() + mileage);

        // 변경된 마일리지 정보 저장
        memberRepository.save(user);
    }

    // 마일리지 가져오는 로직
    public int getUserMileageAmount(int userId) {
        // userId로 회원 정보 조회
        MemberEntity user = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + userId));

        // 유저의 마일리지 양을 반환
        return user.getUserMileage();
    }
}
