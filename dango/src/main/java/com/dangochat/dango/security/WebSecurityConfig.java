package com.dangochat.dango.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    //로그인 없이 접근 가능 경로
    private static final String[] PUBLIC_URLS = {
            "/"                 //메인화면
            , "/images/**"      //이미지 파일 페이지
            , "/css/**"         //css 파일 페이지
            , "/js/**"         //자바스크립트 페이지
            ,"/member/joinForm"
            ,"/member/join"
            ,"/member/idCheck"
            ,"/miynnn"
            ,"/honeybitterchip"
            ,"/leean"
            ,"/hyeonmin"
            ,"/study/**"
    };

    @Bean
    protected SecurityFilterChain config(HttpSecurity http) throws Exception {
        http
            //요청에 대한 권한 설정
            .authorizeHttpRequests(author -> author
                .requestMatchers(PUBLIC_URLS).permitAll()   //모두 접근 허용
                .anyRequest().authenticated()               //그 외의 모든 요청은 인증 필요
            )
            //HTTP Basic 인증을 사용하도록 설정
            .httpBasic(Customizer.withDefaults())
            
            //폼 로그인 설정
            .formLogin(formLogin -> formLogin      //수정할거면 따옴표 안에서만 변경
            		 .loginPage("/member/loginForm")              //로그인폼 페이지 경로
                     .usernameParameter("userId")               //폼의 ID 파라미터 이름
                     .passwordParameter("userPassword")         //폼의 비밀번호 파라미터 이름
                     .loginProcessingUrl("/member/login")         //로그인폼 제출하여 처리할 경로
                     .defaultSuccessUrl("/")                      //로그인 성공 시 이동할 경로
                     .permitAll()                            //로그인 페이지는 모두 접근 허용
            )
            //로그아웃 설정
            .logout(logout -> logout
                    .logoutUrl("/logout")                   //로그아웃 처리 경로
                    .logoutSuccessUrl("/")                  //로그아웃 성공 시 이동할 경로
            );

        http
            .cors(AbstractHttpConfigurer::disable)
            .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    //비밀번호 암호화를 위한 인코더를 빈으로 등록
    @Bean
    public BCryptPasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
