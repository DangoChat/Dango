package com.dangochat.dango.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_completion_rate")
public class UserCompletionRateEntity {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "completion_num")
    private int completionNum;
    
 // 회원 고유 아이디 (외래키)
    @OneToOne  // 각 유저는 하나의 레코드만 가짐
    @JoinColumn(name = "user_id", nullable = false,referencedColumnName = "user_id")
    private MemberEntity user; 
    
    @Column(name = "weekly_points", nullable = false)
    private int weeklyPoints;
    
    @Column(name = "total_points", nullable = false)
    private int totalPoints;
    
    @Column(name = "completion_date", nullable = false)
    private LocalDateTime completionDate = LocalDateTime.now();
    
}

