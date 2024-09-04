package com.dangochat.dango.repository;

import com.dangochat.dango.entity.UserStudyContentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserStudyContentRepository extends JpaRepository<UserStudyContentEntity, String> {
}

