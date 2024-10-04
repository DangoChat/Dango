package com.dangochat.dango.entity.ChatEntitys;

import java.time.LocalDateTime;

import com.dangochat.dango.entity.MemberEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "chat_messages")
public class ChatMessageJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    @Column(name = "chat_messages_id")
    private Long chatMessagesId;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private ChatRoomJpaEntity chatRoom;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private MemberEntity sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private MemberEntity receiver;

    @Column(name = "messages_contents")
    private String messagesContents;

    @Column(name = "messages_sent_date")
    private LocalDateTime messagesSentDate;

    @Column(name = "message_mileage_cost")
    private int messageMileageCost;

     @PrePersist
    protected void onCreate() {
        this.messagesSentDate = LocalDateTime.now();
    }
}