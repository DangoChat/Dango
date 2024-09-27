package com.dangochat.dango.service;

import com.dangochat.dango.entity.MemberEntity;
import com.dangochat.dango.entity.UserMileageEntity;
import com.dangochat.dango.repository.UserMileageRepository;
import com.dangochat.dango.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserMileageService {

    private final UserMileageRepository userMileageRepository;
    private final MemberRepository memberRepository;

    // 마일리지 증가 로직
    public void addMileage(MemberEntity user, int mileage) {
        UserMileageEntity userMileage = userMileageRepository.findByUserId(user)
                .orElse(null);


        // 만약 마일리지 정보가 없으면 새로 생성
        if (userMileage == null) {
            userMileage = new UserMileageEntity();
            userMileage.setUserId(user);
            userMileage.setUserMileageAmount(0);
        }

        // 마일리지 업데이트
        userMileage.setUserMileageAmount(userMileage.getUserMileageAmount() + mileage);

        // 마지막 업데이트 날짜 갱신
        userMileage.setMileageLastUpdated(LocalDateTime.now());

        // 변경된 마일리지 정보 저장
        userMileageRepository.save(userMileage);
    }

    // 마일리지 가져오는 로직
    public int getUserMileageAmount(int userId) {
        // userId로 회원 정보 조회
        MemberEntity user = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + userId));

        // 유저의 마일리지 정보를 조회
        UserMileageEntity userMileage = userMileageRepository.findByUserId(user)
                .orElseThrow(() -> new IllegalArgumentException("No mileage data found for user ID: " + userId));

        // 유저의 마일리지 양을 반환
        return userMileage.getUserMileageAmount();
    }

}
