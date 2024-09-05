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
    private Date mistakeDate;
    private Boolean mistakeResolved;
    private int mistakeCounting;
}
