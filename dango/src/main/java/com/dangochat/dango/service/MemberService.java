package com.dangochat.dango.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.dangochat.dango.dto.MemberDTO;
import com.dangochat.dango.entity.MemberEntity;
import com.dangochat.dango.repository.MemberRepository;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Transactional
public class MemberService {

	//회원정보 DB처리
	private final MemberRepository memberRepository;
	// EmailService를 주입받음
	@Resource(name="emailService")
	private final EmailService emailService;  

    private final PasswordEncoder passwordEncoder;
	
	
	public void join(MemberDTO member) {
		try {
            // 비밀번호를 AES로 암호화
            String encryptedPassword = passwordEncoder.encode(member.getUserPassword());

            // DTO를 Entity로 변환
            MemberEntity memberEntity = MemberEntity.builder()
                    .userEmail(member.getUserEmail())
                    .userPassword(encryptedPassword)  // 암호화된 비밀번호 저장
                    .nickname(member.getNickname())
                    .userPhone(member.getUserPhone())
                    .userNationality(member.getUserNationality())
                    .currentLevel(null)  // 초기에는 null 또는 기본값 설정
                    .userMileage(0)      // 초기 마일리지는 0으로 설정
                    .build();

            // 데이터베이스에 회원 정보 저장
            memberRepository.save(memberEntity);
        } catch (Exception e) {
            e.printStackTrace();
        }

	}
	
	// 아이디 중복 체크 관련 서비스
	public int idCheck(String email) {
		// 중복된 이메일이 있으면 1, 없으면 0을 반환
        boolean exists = memberRepository.existsByUserEmail(email);
        return exists ? 1 : 0;
	}
	
	
	
	
}
