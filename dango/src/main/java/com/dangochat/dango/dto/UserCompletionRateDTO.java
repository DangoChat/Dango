package com.dangochat.dango.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCompletionRateDTO {

    private int completionNum;
    private int userId;
    private int weeklyPoints;
    private int totalPoints;
    private LocalDateTime completionDate;
}

