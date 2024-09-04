package com.dangochat.dango.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user_study_content")
public class UserStudyContentEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // GenerationType.IDENTITY로 변경
    @Column(name = "user_study_record_id")
    private int userStudyRecordId;

    @Column(name = "study_content_id", nullable = false)
    private int studyContentId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "record_study_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date recordStudyDate;

    @Column(name = "record_is_correct", nullable = false)
    private boolean recordIsCorrect;

    @PrePersist
    protected void onCreate() {
        if (this.recordStudyDate == null) {
            this.recordStudyDate = new Date();  // 오늘 날짜로 자동 설정
        }
    }
}
