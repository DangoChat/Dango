package com.dangochat.dango.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserMistakesDTO {
    private int mistakeId;
    private String userId;
    private int studyContentId;
    
    private String content;
    private String pronunciation;
    private String meaning;
    private String type;
    private String level;
    private String example1;
    private String exampleTranslation1;
    private String example2;
    private String exampleTranslation2;
    private Date mistakeDate;
    private Boolean mistakeResolved;
    private int mistakeCounting;
    
}
