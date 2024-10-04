package com.dangochat.dango.repository.ChatRepositorys;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dangochat.dango.entity.ChatEntitys.ChatRoomUserJpaEntity;

@Repository
public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUserJpaEntity, Long> {
}
