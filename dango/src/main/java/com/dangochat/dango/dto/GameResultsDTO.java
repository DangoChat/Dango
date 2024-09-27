package com.dangochat.dango.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameResultsDTO {

    private String userGameId;
    private String gameId;
    private int userId;
    private int opponentId;
    private int userGameScore;
    private boolean userGameResult;
    private LocalDateTime userGameDate;
}
