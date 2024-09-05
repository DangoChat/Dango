package com.dangochat.dango.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
//유저 오답노트 ENTITY
@Table(name = "user_mistakes")
public class UserMistakesEntity {
    //유저 오답노트 아이디
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ID를 자동 증가로 설정
    @Column(name = "mistake_id", nullable = false)
    private int mistakeId;

    //회원 고유 아이디
    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    //공부 내용 고유 아이디
    @Column(name = "study_content_id", nullable = false)
    private int studyContentId;

    //오답 일자
    @Column(name = "user_mistake_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date mistakeDate;

    //오답 노트 해결 여부
    @Column(name = "mistake_resolved")
    private Boolean mistakeResolved;

    //오답 카운팅
    @Column(name = "mistake_counting", nullable = false)
    private int mistakeCounting;

    //사용자 오답 기록 저장
    @PrePersist
    protected void onCreate() {
        if (this.mistakeDate == null) {
            this.mistakeDate = new Date();  // 현재 날짜로 자동 설정
        }
    }
}
