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
                    .userNationality(member.getUserNationality())
                    .currentLevel(null)  // 초기에는 null 또는 기본값 설정
                    .originalLevel(null)
                    .userMileage(0)      // 초기 마일리지는 0으로 설정
                    .userSex(member.getUserSex())
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
	
	// 유저 CurrentLevel정보 가저오는 메서드
	public String getUserCurrentLevel(int userId){
		MemberEntity memberEntity = memberRepository.findById(userId).orElse(null);
		String userLevel = memberEntity.getCurrentLevel();
		return userLevel;
	}

	// original_level 가져오기
    public String getOriginalLevel(int userId) {
        MemberEntity member = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return member.getOriginalLevel();
    }
	
	// current_level과 original_level 둘 다 업데이트
    public void updateUserLevels(Integer userId, String currentLevel, String originalLevel) {
        MemberEntity member = memberRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        member.setCurrentLevel(currentLevel);
        member.setOriginalLevel(originalLevel);
        memberRepository.save(member);
    }

 // current_level만 업데이트
    public void updateCurrentLevel(Integer userId, String currentLevel) {
        MemberEntity member = memberRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        member.setCurrentLevel(currentLevel);
        memberRepository.save(member);
    }

    public MemberEntity findById(int userId) {
        return memberRepository.findById(userId).orElse(null);
    }

    public void save(MemberEntity member) {
        memberRepository.save(member);
    }

	public MemberDTO getMemberInfo(String memberEmail){
        MemberEntity entity = memberRepository.findByUserEmail(memberEmail);
        MemberDTO dto = MemberDTO.builder()
            .userEmail(entity.getUserEmail())
            .userId(entity.getUserId())
            .userMileage(entity.getUserMileage())
            .userNationality(entity.getUserNationality())
            .userPassword(entity.getUserPassword())
            .currentLevel(entity.getCurrentLevel())
            .originalLevel(entity.getOriginalLevel())
            .nickname(entity.getNickname())
            .userSex(entity.getUserSex())
            .build();
        return dto;
    }

	public String getUserNationality(int id) {
		
		return memberRepository.findUserNationalityById(id);
	}
	
}
