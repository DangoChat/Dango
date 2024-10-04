package com.dangochat.dango.repository.ChatRepositorys;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dangochat.dango.entity.ChatEntitys.ChatMessageJpaEntity;
import com.dangochat.dango.entity.ChatEntitys.ChatRoomJpaEntity;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoomJpaEntity, Long> {
    void save(ChatMessageJpaEntity chatRoom);
}