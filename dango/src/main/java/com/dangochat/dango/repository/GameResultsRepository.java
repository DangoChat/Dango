package com.dangochat.dango.repository;

import com.dangochat.dango.entity.GameResultsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameResultsRepository extends JpaRepository<GameResultsEntity, String> {

    // 유저 ID로 게임 결과 목록 조회
    List<GameResultsEntity> findByUserUserId(int userId);
}
