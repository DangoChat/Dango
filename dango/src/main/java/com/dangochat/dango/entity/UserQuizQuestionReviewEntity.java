package com.dangochat.dango.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "user_quiz_question_review")
public class UserQuizQuestionReviewEntity {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_quiz_question_id")
    private int userQuizQuestionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    private MemberEntity user;  // 연관된 UserEntity

    @Column(name = "quiz_study_date", nullable = false)
    private LocalDateTime quizStudyDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "quiz_type", nullable = false)
    private QuizType quizType;  // "daily" 또는 "weekly"

    @Column(name = "quiz_content", columnDefinition = "TEXT", nullable = false)
    private String quizContent;

    @Column(name = "quiz_status", nullable = false)
    private Boolean quizStatus;  // 퀴즈 상태 (true 또는 false)

    // 사용자 오답 기록 저장 시 자동으로 시간 설정
    @PrePersist
    protected void onCreate() {
        if (this.quizStudyDate == null) {
            this.quizStudyDate = LocalDateTime.now();  // 현재 시간으로 자동 설정
        }
    }
}
