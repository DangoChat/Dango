package com.dangochat.dango.entity.ChatEntitys;

import com.dangochat.dango.entity.MemberEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "chat_room_users")
public class ChatRoomUserJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    @Column(name = "room_user_id")
    private Long roomUserId;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private ChatRoomJpaEntity chatRoom;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private MemberEntity user;

}