package com.dangochat.dango.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dangochat.dango.entity.MemberEntity;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, String>{

	
}
