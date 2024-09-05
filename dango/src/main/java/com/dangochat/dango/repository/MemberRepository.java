package com.dangochat.dango.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dangochat.dango.entity.MemberEntity;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Integer> {
	
	
    // 이메일로 회원을 찾는 메서드
    MemberEntity findByUserEmail(String userEmail);

    // 토큰으로 회원을 찾는 메서드
    MemberEntity findByToken(String token);
}
