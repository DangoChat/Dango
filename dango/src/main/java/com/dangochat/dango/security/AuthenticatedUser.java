package com.dangochat.dango.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor

/*
 * AuthenticatedUser 클래스는 사용자 정보와 권한을 관리하며, Spring Security에서 사용자 인증과
 * 권한 부여에 사용됩니다. UserDetails 인터페이스를 구현함으로써 Spring Security가 이 클래스의 인스턴스를 인증 과정에서
 * 사용할 수 있습니다. 필드와 메서드를 통해 사용자 ID, 비밀번호, 역할, 계정 상태 등을 관리합니다.
 */
public class AuthenticatedUser implements UserDetails {

    private static final long serialVersionUID = 1562050567301951305L;

    private String id;       // 사용자 ID (userId 필드에 매핑)
    private String password; // 사용자 비밀번호 (userPassword 필드에 매핑)
    private String name;     // 사용자의 이름 또는 닉네임 (nickname 필드에 매핑)

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 기본 권한을 부여 ("ROLE_USER" 등, 필요에 따라 수정 가능)
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return id;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;  // 계정이 만료되지 않았음을 나타냄
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;  // 계정이 잠기지 않았음을 나타냄
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // 자격 증명이 만료되지 않았음을 나타냄
    }


    // @Override
    // public boolean isEnabled() {
    //     return enabled;  // 계정 활성화 여부
    // }

}
