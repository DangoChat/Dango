package com.dangochat.dango.service;

import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
	//비밀번호 암호화
	private final BCryptPasswordEncoder passwordEncoder;
	// EmailService를 주입받음
	@Resource(name="emailService")
	private final EmailService emailService;  
	
	
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

	public boolean sendPasswordResetLink(String email) {
		MemberEntity member = memberRepository.findByUserEmail(email);
	    if (member == null) {
	        return false; // 이메일이 존재하지 않음
	    }

	    // 비밀번호 재설정 토큰 생성
	    String token = UUID.randomUUID().toString();
	    // 토큰 저장 로직 (예: 데이터베이스에 저장하거나 별도의 서비스에 저장)

	    // 비밀번호 재설정 URL 생성
	    String resetUrl = "http://localhost:8888/member/resetPassword?token=" + token;

	    // 이메일 전송 로직
	    // 예: JavaMailSender를 사용하여 이메일 전송
	    emailService.sendEmail(email, "비밀번호 재설정", 
	        "비밀번호 재설정을 위해 아래 링크를 클릭하세요:\n" + resetUrl);

	    return true;
	}

	
	
	public boolean resetPassword(String token, String newPassword) {
		// 토큰을 이용해 사용자 찾기
	    MemberEntity member = findMemberByToken(token);
	    if (member == null) {
	        return false; // 토큰이 유효하지 않음
	    }

	    // 새 비밀번호 암호화
	    String encodedPassword = passwordEncoder.encode(newPassword);
	    member.setUserPassword(encodedPassword);

	    // 데이터베이스에 저장
	    memberRepository.save(member);

	    // 토큰 삭제 또는 무효화 로직

	    return true;
	}

	private MemberEntity findMemberByToken(String token) {
	    // 토큰을 이용해 사용자 찾는 로직 구현
	    // 예: 데이터베이스에서 토큰으로 사용자 조회
	    return memberRepository.findByToken(token);
	}
	
	
	
	
}
