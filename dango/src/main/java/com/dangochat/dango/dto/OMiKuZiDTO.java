package com.dangochat.dango.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OMiKuZiDTO {

	private Integer omikuziId;
    private String omikuziResult;
    private String krDescription;  // 한국어 결과 설명
    private String jpDescription;  // 일본어 결과 설명
}
