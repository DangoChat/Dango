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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_study_record_id")
    private int userStudyRecordId;

    // Many-to-One 관계 설정 (User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private MemberEntity user;

    // Many-to-One 관계 설정 (StudyContent)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_content_id", nullable = false)
    private StudyEntity studyContent;

    @Column(name = "record_study_date", nullable = true)
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
