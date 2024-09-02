package com.dangochat.dango.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.dangochat.dango.entity.MemberEntity;
import com.dangochat.dango.repository.MemberRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticatedUserDetailsService implements UserDetailsService {

    private final BCryptPasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 전달받은 아이디로 회원정보 DB에서 조회, 없으면 예외 처리
        MemberEntity entity = memberRepository.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException("회원정보가 없습니다."));

        // 있으면 그 정보로 AuthenticatedUser 객체 생성하여 리턴
        AuthenticatedUser user = AuthenticatedUser.builder()
                .id(entity.getUserId())                       // MemberEntity의 userId 필드 사용
                .password(entity.getUserPassword())           // MemberEntity의 userPassword 필드 사용
                .name(entity.getNickname())                   // MemberEntity의 nickname 필드 사용
                .build();

        log.debug("인증정보: {}", user);
        return user;
    }
}
