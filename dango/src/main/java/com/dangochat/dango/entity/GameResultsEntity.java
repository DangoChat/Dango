package com.dangochat.dango.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameResultsEntity {

    @Id
    @Column(name = "user_game_id", nullable = false, length = 200)
    private String userGameId;

    @Column(name = "game_id", nullable = false, length = 200)
    private String gameId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private MemberEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opponent_id", nullable = false)
    private MemberEntity opponent;

    @Column(name = "user_game_score", nullable = false, columnDefinition = "INT DEFAULT 0")
    private int userGameScore;

    @Column(name = "user_game_result", nullable = false)
    private boolean userGameResult;

    @Column(name = "user_game_date", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime userGameDate;
}
