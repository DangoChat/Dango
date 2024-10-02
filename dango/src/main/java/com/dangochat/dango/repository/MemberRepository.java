 package com.dangochat.dango.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dangochat.dango.entity.MemberEntity;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Integer> {
	
	 // 이메일로 회원을 찾는 메서드
    MemberEntity findByUserEmail(String userEmail);

    // 이메일로 회원을 찾는 메서드
    boolean existsByUserEmail(String userEmail);
    
    // 마일리지 추가 메서드
    @Modifying
    @Query("UPDATE MemberEntity m SET m.userMileage = m.userMileage + :points WHERE m.userId = :userId")
    void addMileage(@Param("userId") Integer userId, @Param("points") int points);

}
