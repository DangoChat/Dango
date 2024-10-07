package com.dangochat.dango.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_ranking")
public class UserRankingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_ranking_id")
    private int userRankingId;  // Long 타입으로 수정

    @Column(name = "user_id", nullable = false)
    private int userId;

    @Column(name = "ranking_category", nullable = false)
    private String rankingCategory;

    @Column(name = "ranking_point", nullable = false)
    private int rankingPoint;

    @Column(name = "ranking_date", nullable = false)
    private LocalDateTime rankingDate;
}
