package com.dangochat.dango.repository;

import com.dangochat.dango.entity.LevelsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LevelsRepository extends JpaRepository<LevelsEntity, String> {

}