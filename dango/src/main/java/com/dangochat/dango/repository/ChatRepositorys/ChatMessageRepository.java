package com.dangochat.dango.repository.ChatRepositorys;

import org.springframework.stereotype.Repository;

import com.dangochat.dango.entity.ChatEntitys.ChatMessageJpaEntity;

import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageJpaEntity, Long>{
}
