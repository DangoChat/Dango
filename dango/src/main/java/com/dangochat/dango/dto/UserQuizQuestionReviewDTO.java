package com.dangochat.dango.dto;

import com.dangochat.dango.entity.QuizType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserQuizQuestionReviewDTO {

    private int userQuizQuestionId;  // 기본 키
    private int userId;  // UserEntity의 ID를 저장
    private LocalDateTime quizStudyDate;  // 퀴즈 학습 날짜
    private QuizType quizType;  // "daily" 또는 "weekly"
    private String quizContent;  // 퀴즈 내용 (JSON 형식으로 저장될 퀴즈 문제)
    private Boolean quizStatus;  // 퀴즈 상태 (맞았는지 틀렸는지 표시)
    
    
    public Boolean isQuizStatus() {
        return quizStatus;
    }
}
