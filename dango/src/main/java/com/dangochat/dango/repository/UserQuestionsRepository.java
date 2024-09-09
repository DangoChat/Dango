package com.dangochat.dango.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.dangochat.dango.entity.UserQuestionsEntity;

@Repository
public interface UserQuestionsRepository extends JpaRepository<UserQuestionsEntity, String> {

}
