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
@Table(name = "user_mistakes")
public class UserMistakesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ID를 자동 증가로 설정
    @Column(name = "mistake_id", nullable = false)
    private int mistakeId;

    @Column(name = "user_id", nullable = false, length = 100)
    private int userId;

    @Column(name = "study_content_id", nullable = false)
    private int studyContentId;

    @Column(name = "user_mistake_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date mistakeDate;

    @Column(name = "mistake_resolved")
    private Boolean mistakeResolved;

    @Column(name = "mistake_counting", nullable = false)
    private int mistakeCounting;

    @PrePersist
    protected void onCreate() {
        if (this.mistakeDate == null) {
            this.mistakeDate = new Date();  // 현재 날짜로 자동 설정
        }
    }
}
