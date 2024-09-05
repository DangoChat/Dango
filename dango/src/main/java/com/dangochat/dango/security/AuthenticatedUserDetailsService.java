package com.dangochat.dango.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {
        // 전달받은 이메일로 회원정보 DB에서 조회
        MemberEntity entity = memberRepository.findByUserEmail(userEmail);
        
        // null 체크 후 예외 처리
        if (entity == null) {
            throw new UsernameNotFoundException("회원정보가 없습니다.");
        }

        // 이메일을 포함한 AuthenticatedUser 객체 생성
        AuthenticatedUser user = AuthenticatedUser.builder()
                .id(entity.getUserId())                       // MemberEntity의 userId 필드 사용
                .password(entity.getUserPassword())           // MemberEntity의 userPassword 필드 사용
                .name(entity.getNickname())                   // MemberEntity의 nickname 필드 사용
                .email(entity.getUserEmail())                 // MemberEntity의 userEmail 필드 사용
                .build();

        log.debug("인증정보: {}", user);
        return user;
    }


}
