package com.dangochat.dango.service.ChatServices;

import java.util.List;

import org.springframework.stereotype.Service;

import com.dangochat.dango.dto.chatDTOs.ChatMessageCreateCommand;
import com.dangochat.dango.dto.chatDTOs.ChatMessageRequest;
import com.dangochat.dango.dto.chatDTOs.ChatMessageResponse;
import com.dangochat.dango.entity.MemberEntity;
import com.dangochat.dango.entity.ChatEntitys.ChatMessageJpaEntity;
import com.dangochat.dango.entity.ChatEntitys.ChatRoomJpaEntity;
import com.dangochat.dango.repository.MemberRepository;
import com.dangochat.dango.repository.ChatRepositorys.ChatRoomRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageService {
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final ChatRoomService chatRoomService;

    // 채팅 메시지 생성 및 저장
    public Long createChatMessage(ChatMessageJpaEntity chatMessage) {
        ChatRoomJpaEntity chatRoom = chatRoomRepository.findById(chatMessage.getChatRoom().getRoomId())
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        chatRoom.createMessage(chatMessage); // 채팅방에 메시지 추가
        chatRoomRepository.save(chatRoom);   // 메시지 저장

        return chatMessage.getChatMessagesId();
    }

    // 메시지 로드
    public List<ChatMessageJpaEntity> getMessagesForRoom(Long roomId) {
        ChatRoomJpaEntity chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));
        return chatRoom.getMessages();
    }

    // userId로 UserJpaEntity 찾기
    public MemberEntity getUserById(int userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public ChatMessageResponse sendMessage(Long roomId, ChatMessageRequest chatMessage) {
        ChatMessageCreateCommand command = ChatMessageCreateCommand.builder()
                .roomId(roomId)
                .content(chatMessage.text())
                .from(chatMessage.from())
                .to(chatMessage.to())
                .build();

        Long chatId = createChatMessage(
            ChatMessageJpaEntity.builder()
                .chatRoom(chatRoomService.loadById(roomId))
                .messagesContents(command.content())
                .sender(getUserById(command.from()))
                .receiver(getUserById(command.to()))
                .build()
        );

        return ChatMessageResponse.builder()
                .id(chatId)
                .content(chatMessage.text())
                .writer(chatMessage.from())
                .build();
    }   

}