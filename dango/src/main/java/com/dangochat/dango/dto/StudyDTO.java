package com.dangochat.dango.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 학습 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudyDTO {
    private int studyContentId;
    private String content;
    private String pronunciation;
    private String meaning;
    private String type;
    private String example;
}
