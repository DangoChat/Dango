package com.dangochat.dango.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserQuestionsDTO { //유저 문제
    private String questionId;              //퀴즈 문제 고유 ID
    private String quizId;                  //퀴즈 고유 ID
    private Integer studyContentId;         //공부 내용 고유
    private Boolean questionIsCorrect;      //퀴즈 정답 여부
}

