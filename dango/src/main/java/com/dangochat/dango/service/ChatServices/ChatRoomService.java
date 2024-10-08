package com.dangochat.dango.service.ChatServices;

import com.dangochat.dango.entity.MemberEntity;
import com.dangochat.dango.entity.ChatEntitys.ChatRoomJpaEntity;
import com.dangochat.dango.entity.ChatEntitys.ChatRoomUserJpaEntity;
import com.dangochat.dango.repository.MemberRepository;
import com.dangochat.dango.repository.ChatRepositorys.ChatRoomRepository;
import com.dangochat.dango.repository.ChatRepositorys.ChatRoomUserRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

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

    public List<ChatRoomJpaEntity> getChatRoomsForUser(int userId) {
        // 필요한 로직을 사용하여 사용자가 속한 채팅방 목록을 가져옵니다.
        return chatRoomRepository.findChatRoomsByUserId(userId);
    }

    public MemberEntity findRandomUserByNationality(String nationality) {
        return memberRepository.findRandomUserByNationality(nationality);
    }

    public Long createChatRoomWithPartner(MemberEntity partner, int userId) {
        ChatRoomJpaEntity chatRoom = new ChatRoomJpaEntity();
        chatRoom.setRoomName(userId + " Chat with " + partner.getNickname());
        
        // 새 채팅방을 저장하고, 현재 사용자와 상대방을 방에 추가합니다.
        chatRoomRepository.save(chatRoom);
        addUserToRoom(chatRoom.getRoomId(), userId);
        addUserToRoom(chatRoom.getRoomId(), partner.getUserId());
    
        return chatRoom.getRoomId();
    }
    public MemberEntity findUserById(int userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }

    // 새 채팅방을 생성하는 메서드
    public Long createNewChatRoom(String roomName) {
        ChatRoomJpaEntity chatRoom = ChatRoomJpaEntity.builder()
            .roomName(roomName)
            .build();
        
        // 새 채팅방을 저장
        chatRoomRepository.save(chatRoom);
        return chatRoom.getRoomId();
    }

}
