package com.dangochat.dango.repository;

import com.dangochat.dango.entity.OMiKuZiEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OMiKuZiRepository extends JpaRepository<OMiKuZiEntity, Integer> {
	
	
	
}
