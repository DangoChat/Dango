package com.dangochat.dango.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Entity
    @Table(name = "quiz_questions") // 퀴즈 문제들
    public class UserQuestionsEntity {

        //퀴즈 문제 고유 ID
        @Id
        @Column(name = "question_id", length = 200, nullable = false)
        private String questionId;

        //퀴즈 고유 ID
        @Column(name = "quiz_id", length = 200, nullable = false)
        private String quizId;

        //공부 내용 고유
        @Column(name = "study_content_id", nullable = false)
        private Integer studyContentId;

        //퀴즈 정답 여부
        @Column(name = "question_is_correct")
        private Boolean questionIsCorrect;
    }
    /*
    @ManyToOne
    @JoinColumn(name = "quiz_id", insertable = false, updatable = false)
    private UserQuizzes quiz;  // Quiz 엔티티와 관계

    @ManyToOne
    @JoinColumn(name = "study_content_id", insertable = false, updatable = false)
    private StudyContent studyContent;  // StudyContent 엔티티와 관계
    */