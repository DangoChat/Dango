package com.dangochat.dango.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users") // 테이블 이름은 소문자 "users"로 일치시킴
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT에 맞는 전략 추가
    @Column(name = "user_id")
    private int userId;  // int로 설정, AUTO_INCREMENT 매핑

    @Column(name = "user_email", length = 200, nullable = false)
    private String userEmail;

    @Column(name = "user_password", length = 200, nullable = false)
    private String userPassword;

    @Column(name = "nickname", length = 200, nullable = false)
    private String nickname;

    @Column(name = "user_phone", length = 200, nullable = false)
    private String userPhone;

    @Column(name = "user_nationality", length = 20, nullable = false)
    private String userNationality;

    @Column(name = "current_level", length = 50, nullable = true)
    private String currentLevel;

    @Column(name = "user_mileage", nullable = false, columnDefinition = "INT DEFAULT 0")
    private int userMileage;

    @Column(name = "token", length = 200, nullable = true)
    private String token;
}
