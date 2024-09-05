package com.dangochat.dango.security;

import java.util.Collection;
import java.util.Collections;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
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
    
    private int id;
    private String password;
    private String name;
    private String email;  // 이메일 필드 추가

    private static final long serialVersionUID = 1562050567301951305L;

//    private String id;       // 사용자 ID (userId 필드에 매핑)
//    private String password; // 사용자 비밀번호 (userPassword 필드에 매핑)
//    private String name;     // 사용자의 이름 또는 닉네임 (nickname 필드에 매핑)

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList(); // 권한을 설정할 수 있다면 GrantedAuthority 리스트를 반환합니다.
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.name;  // 이름 또는 이메일을 반환하도록 설정할 수 있습니다
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
