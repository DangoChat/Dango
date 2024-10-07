package com.dangochat.dango.repository.ChatRepositorys;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dangochat.dango.entity.ChatEntitys.ChatMessageJpaEntity;
import com.dangochat.dango.entity.ChatEntitys.ChatRoomJpaEntity;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoomJpaEntity, Long> {
    void save(ChatMessageJpaEntity chatRoom);

    @Query("SELECT cr FROM ChatRoomJpaEntity cr JOIN cr.users cru WHERE cru.user.userId = :userId")
    List<ChatRoomJpaEntity> findChatRoomsByUserId(@Param("userId") int userId);
}