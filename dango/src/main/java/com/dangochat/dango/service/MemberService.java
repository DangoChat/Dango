package com.dangochat.dango.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.dangochat.dango.dto.MemberDTO;
import com.dangochat.dango.entity.MemberEntity;
import com.dangochat.dango.repository.MemberRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Transactional
public class MemberService {

	//회원정보 DB처리
	private final MemberRepository memberRepository;
	//비밀번호 암호화
	private final BCryptPasswordEncoder passwordEncoder;
	
	public void join(MemberDTO member) {
		  // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(member.getUserPassword());

        // DTO를 Entity로 변환
        MemberEntity memberEntity = MemberEntity.builder()
                .userId(member.getUserId())
                .userEmail(member.getUserEmail())
                .userPassword(encodedPassword)
                .nickname(member.getNickname())
                .userPhone(member.getUserPhone())
                .userNationality(member.getUserNationality())
                .currentLevel(null)  // 초기에는 null 또는 기본값 설정
                .userMileage(0)      // 초기 마일리지는 0으로 설정
                .build();

        // 데이터베이스에 회원 정보 저장
        memberRepository.save(memberEntity);
	}
	
	
}
