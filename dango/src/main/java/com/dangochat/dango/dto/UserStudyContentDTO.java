package com.dangochat.dango.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStudyContentDTO {
	 private int userStudyRecordId;
	    private int studyContentId;
	    private int userId;
	    private java.util.Date recordStudyDate = new java.util.Date();
	    private boolean recordIsCorrect;
	    private String content;
	    private String pronunciation;
	    private String meaning;
	    private String type;
	    private String example1;
	    private String exampleTranslation1;
	    private String example2;
	    private String exampleTranslation2;
}

