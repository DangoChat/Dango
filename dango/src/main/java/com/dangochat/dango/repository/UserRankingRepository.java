package com.dangochat.dango.repository;

import com.dangochat.dango.entity.UserRankingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRankingRepository extends JpaRepository<UserRankingEntity, String> {

    /**
     * 사용자 ID와 카테고리로 사용자 랭킹을 조회하는 메서드
     *
     * @param userId 사용자 ID
     * @param category 랭킹 카테고리
     * @return UserRanking 객체
     */
    UserRankingEntity findByUserIdAndRankingCategory(int userId, String category);
}
