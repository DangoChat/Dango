package com.dangochat.dango.service.ChatServices;

import com.dangochat.dango.entity.MemberEntity;
import com.dangochat.dango.entity.ChatEntitys.ChatRoomJpaEntity;
import com.dangochat.dango.entity.ChatEntitys.ChatRoomUserJpaEntity;
import com.dangochat.dango.repository.MemberRepository;
import com.dangochat.dango.repository.ChatRepositorys.ChatRoomRepository;
import com.dangochat.dango.repository.ChatRepositorys.ChatRoomUserRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomUserRepository chatRoomUserRepository;
    private final MemberRepository memberRepository;

    public Long createChatRoom(String roomName) {
        ChatRoomJpaEntity chatRoom = new ChatRoomJpaEntity();
        chatRoom.setRoomName(roomName);
        chatRoomRepository.save(chatRoom);
        return chatRoom.getRoomId();
    }

    public void addUserToRoom(Long roomId, int userId) {
        ChatRoomJpaEntity chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));
        MemberEntity user = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ChatRoomUserJpaEntity roomUser = new ChatRoomUserJpaEntity();
        roomUser.setChatRoom(chatRoom);
        roomUser.setUser(user);
        chatRoomUserRepository.save(roomUser);
    }

    public ChatRoomJpaEntity loadById(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));
    }

    public List<ChatRoomJpaEntity> searchAllRooms() {
        return chatRoomRepository.findAll();
    }

     // 모든 채팅방 조회
    public List<ChatRoomJpaEntity> getAllChatRooms() {
        return chatRoomRepository.findAll();
    }
}
