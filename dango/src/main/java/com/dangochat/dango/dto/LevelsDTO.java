package com.dangochat.dango.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 레벨 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LevelsDTO {
    private String level;                // 레벨 이름
    private String levelExamType;        // 레벨 시험 타입
    private String levelDescription;     // 레벨 설명

    // 단일 필드 생성자 (레벨 이름만 필요할 때 사용)
    public LevelsDTO(String level) {
        this.level = level;
    }
}
