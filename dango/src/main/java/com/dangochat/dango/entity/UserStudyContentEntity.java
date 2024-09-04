package com.dangochat.dango.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "user_study_content")
public class UserStudyContentEntity {

    @Id
    @Column(name = "user_study_record_id")
    private String userStudyRecordId;

    @Column(name = "study_content_id", nullable = false)
    private int studyContentId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "record_study_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date recordStudyDate = new java.util.Date();

    @Column(name = "record_is_correct", nullable = false)
    private boolean recordIsCorrect;
}
