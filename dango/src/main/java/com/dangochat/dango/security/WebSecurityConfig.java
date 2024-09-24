package com.dangochat.dango.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    // application.properties 파일에서 주입된 CORS 허용 도메인
    @Value("${security.cors-allowed-origins}")
    private String allowedOrigins;

    private static final String[] PUBLIC_URLS = {
            "/", "/images/**", "/css/**", "/js/**", "/member/joinForm", "/member/join", 
            "/member/idCheck", "/member/passwordSearch", "/miynnn", "/honeybitterchip", 
            "/leean", "/hyeonmin", "/study/**", "/mail/**", "/api/member/**"
    };

    @Bean
    protected SecurityFilterChain config(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(PUBLIC_URLS).permitAll()   // 모두 접근 허용
                .anyRequest().authenticated()               // 나머지 요청은 인증 필요
            )
            .httpBasic(Customizer.withDefaults())           // HTTP Basic 인증 사용

            // 폼 로그인 설정 유지
            .formLogin(formLogin -> formLogin
                    .loginPage("/member/loginForm")
                    .usernameParameter("userEmail")
                    .passwordParameter("userPassword")
                    .loginProcessingUrl("/member/login")
                    .defaultSuccessUrl("/")
                    .permitAll()
            )
            .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/")
            )
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/member/**"))  // REST API 경로는 CSRF 비활성화
            .cors(Customizer.withDefaults());  // CORS 설정 활성화

        return http.build();
    }

    // CORS 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins)); // 프로파일에 따라 도메인 설정
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true); // 자격 증명 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // AuthenticationManager를 빈으로 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new AESPasswordEncoder();
    }
}
