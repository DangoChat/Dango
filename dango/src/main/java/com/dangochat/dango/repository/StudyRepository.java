package com.dangochat.dango.repository;

import com.dangochat.dango.entity.StudyEntity;
import org.springframework.data.jpa.repository.JpaRepository;  // JpaRepository를 import
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyRepository extends JpaRepository<StudyEntity, Integer> {
    List<StudyEntity> findByLevel(String level);
}