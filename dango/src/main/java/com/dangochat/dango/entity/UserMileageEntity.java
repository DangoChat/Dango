package com.dangochat.dango.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_mileage")
public class UserMileageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // INT AUTO_INCREMENT에 맞는 전략 추가
    @Column(name = "user_mileage_id", nullable = false, updatable = false)
    private int userMileageId; // AUTO_INCREMENT INT 타입으로 변경

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private MemberEntity userId;// 회원 고유 아이디

    @Column(name = "user_mileage_amount", nullable = false)
    private int userMileageAmount; // 마일리지 양

    @Column(name = "mileage_last_updated", nullable = false)
    private LocalDateTime mileageLastUpdated = LocalDateTime.now(); //마지막 업데이트 날짜
}
