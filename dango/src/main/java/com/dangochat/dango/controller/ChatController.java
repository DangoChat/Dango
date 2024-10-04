package com.dangochat.dango.controller;

import com.dangochat.dango.dto.chatDTOs.ChatMessageCreateCommand;
import com.dangochat.dango.dto.chatDTOs.ChatMessageRequest;
import com.dangochat.dango.dto.chatDTOs.ChatMessageResponse;
import com.dangochat.dango.entity.ChatEntitys.ChatMessageJpaEntity;
import com.dangochat.dango.entity.ChatEntitys.ChatRoomJpaEntity;
import com.dangochat.dango.service.ChatServices.ChatMessageService;
import com.dangochat.dango.service.ChatServices.ChatRoomService;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final ChatMessageService chatMessageService;
    private final ChatRoomService chatRoomService;

    /**
     * STOMP를 사용하여 채팅방에 메시지 전송
     * @param roomId 채팅방 ID
     * @param chatMessage 전송된 메시지
     * @return ChatMessageResponse
     */
    @MessageMapping("/chat/rooms/{roomId}/send")
    @SendTo("/topic/public/rooms/{roomId}")
    public ChatMessageResponse sendMessage(@DestinationVariable Long roomId, @Payload ChatMessageRequest chatMessage) {
        ChatMessageCreateCommand command = ChatMessageCreateCommand.builder()
                .content(chatMessage.text())
                .from(chatMessage.from())
                .to(chatMessage.to())  // 수신자 추가
                .roomId(roomId)
                .build();

        // 메시지를 DB에 저장
        Long chatId = chatMessageService.createChatMessage(
            ChatMessageJpaEntity.builder()
                .chatRoom(chatRoomService.loadById(roomId))
                .messagesContents(command.content())
                .sender(chatMessageService.getUserById(command.from())) // 발신자
                .receiver(chatMessageService.getUserById(command.to())) // 수신자
                .build()
        );

        // 메시지 응답 생성 및 반환
        return ChatMessageResponse.builder()
                .id(chatId)
                .content(chatMessage.text())
                .writer(chatMessage.from())  // 발신자의 userId를 int로 반환
                .build();
    }

    /**
     * 새로운 채팅방 생성
     * @param roomName 생성할 채팅방의 이름
     * @return 생성된 채팅방 ID
     */
    // @PostMapping("/chat/rooms")
    // public ResponseEntity<Long> createRoom(@RequestParam String roomName) {
    //     Long roomId = chatRoomService.createChatRoom(roomName);
    //     return ResponseEntity.ok(roomId);
    // }

    /**
     * 채팅방에 사용자 추가
     * @param roomId 채팅방 ID
     * @param userId 추가할 사용자 ID (int 타입)
     * @return HTTP 200 OK
     */
    @PostMapping("/chat/rooms/{roomId}/users")
    public ResponseEntity<Void> addUserToRoom(@PathVariable Long roomId, @RequestParam int userId) {
        chatRoomService.addUserToRoom(roomId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * 특정 채팅방의 메시지 목록 가져오기
     * @param roomId 채팅방 ID
     * @return 채팅 메시지 목록
     */
    @GetMapping("/chat/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getMessagesForRoom(@PathVariable Long roomId) {
        List<ChatMessageJpaEntity> messages = chatMessageService.getMessagesForRoom(roomId);
        List<ChatMessageResponse> responseList = messages.stream().map(message -> ChatMessageResponse.builder()
                .id(message.getChatMessagesId())
                .content(message.getMessagesContents())
                .writer(message.getSender().getUserId()) // 발신자의 userId를 int로 반환
                .build()).collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

    // 테스트
    // 채팅방 목록 및 생성
    @GetMapping("/chat/rooms")
    public String getChatRooms(Model model) {
        List<ChatRoomJpaEntity> chatRooms = chatRoomService.getAllChatRooms();
        model.addAttribute("chatRooms", chatRooms);
        return "chatRooms";  // chatRooms.html로 이동
    }

    @PostMapping("/chat/rooms")
    public String createRoom(@RequestParam String roomName) {
        chatRoomService.createChatRoom(roomName);
        return "redirect:/chat/rooms";
    }

    // 특정 채팅방으로 이동
    @GetMapping("/chat/rooms/{roomId}")
    public String enterRoom(@PathVariable Long roomId, Model model) {
        ChatRoomJpaEntity room = chatRoomService.loadById(roomId);
        model.addAttribute("roomId", roomId);
        model.addAttribute("roomName", room.getRoomName());
        return "chatRoom";  // chatRoom.html로 이동
    }

}
