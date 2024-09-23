package com.dangochat.dango.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "Study_Content")
public class StudyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_content_id")
    private int studyContentId;

    @Column(name = "study_content_content", length = 2000, nullable = false)
    private String content;

    @Column(name = "pronunciation", length = 200)
    private String pronunciation;

    @Column(name = "study_content_meaning", length = 200)
    private String meaning;

    @Column(name = "level", length = 200)
    private String level;

    @Column(name = "study_content_type", length = 100, nullable = false)
    private String type;

    @Column(name = "study_content_example1", length = 2000)
    private String example1;  // 예문1

    @Column(name = "example_translation1", length = 2000)
    private String exampleTranslation1;  // 예문 번역1

    @Column(name = "study_content_example2", length = 2000)
    private String example2;  // 예문2

    @Column(name = "example_translation2", length = 2000)
    private String exampleTranslation2;  // 예문 번역2

}