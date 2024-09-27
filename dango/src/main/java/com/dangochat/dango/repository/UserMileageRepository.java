package com.dangochat.dango.repository;

import com.dangochat.dango.entity.MemberEntity;
import com.dangochat.dango.entity.UserMileageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserMileageRepository extends JpaRepository<UserMileageEntity, Integer> {

    // userId로 UserMileageEntity 조회하는 메서드
    Optional<UserMileageEntity> findByUserId(MemberEntity user);
}
