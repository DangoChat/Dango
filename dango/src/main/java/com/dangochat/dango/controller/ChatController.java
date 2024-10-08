package com.dangochat.dango.controller;

import com.dangochat.dango.dto.chatDTOs.ChatMessageCreateCommand;
import com.dangochat.dango.dto.chatDTOs.ChatMessageRequest;
import com.dangochat.dango.dto.chatDTOs.ChatMessageResponse;
import com.dangochat.dango.dto.chatDTOs.ChatRoomResponse;
import com.dangochat.dango.dto.chatDTOs.ChatRoomResponse.ChatRoomUserResponse;
import com.dangochat.dango.entity.MemberEntity;
import com.dangochat.dango.entity.ChatEntitys.ChatMessageJpaEntity;
import com.dangochat.dango.entity.ChatEntitys.ChatRoomJpaEntity;
import com.dangochat.dango.repository.MemberRepository;
import com.dangochat.dango.service.MemberService;
import com.dangochat.dango.service.ChatServices.ChatMessageService;
import com.dangochat.dango.service.ChatServices.ChatRoomService;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private final MemberService memberService;

    /**
     * STOMP를 사용하여 채팅방에 메시지 전송
     * @param roomId 채팅방 ID
     * @param chatMessage 전송된 메시지
     * @return ChatMessageResponse
     */
    @MessageMapping("/chat/rooms/{roomId}/send")
    @SendTo("/topic/rooms/{roomId}")
    public ChatMessageResponse sendMessage(@DestinationVariable Long roomId, @Payload ChatMessageRequest chatMessage) {
        ChatMessageCreateCommand command = ChatMessageCreateCommand.builder()
                .content(chatMessage.text())
                .from(chatMessage.from())
                .to(chatMessage.to())
                .roomId(roomId)
                .build();

        // 메시지를 DB에 저장
        Long chatId = chatMessageService.createChatMessage(
            ChatMessageJpaEntity.builder()
                .chatRoom(chatRoomService.loadById(roomId))
                .messagesContents(command.content())
                .sender(chatMessageService.getUserById(command.from()))
                .receiver(chatMessageService.getUserById(command.to()))
                .build()
        );
        memberService.updateMileage(command.from(), -5);
        System.out.println("mileage done??? "+ memberService.getUserMileage(command.from()));

        // 메시지 응답 생성 및 반환
        return ChatMessageResponse.builder()
                .id(chatId)
                .content(chatMessage.text())
                .writer(chatMessage.from())
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
    public void createRoom(@RequestParam String roomName) {
        chatRoomService.createChatRoom(roomName);
        // return "redirect:/chat/rooms";
    }

    // 특정 채팅방으로 이동
    @GetMapping("/chat/rooms/{roomId}")
    public String enterRoom(@PathVariable Long roomId, Model model) {
        ChatRoomJpaEntity room = chatRoomService.loadById(roomId);
        model.addAttribute("roomId", roomId);
        model.addAttribute("roomName", room.getRoomName());
        return "chatRoom";  // chatRoom.html로 이동
    }

    // 특정 사용자가 속한 채팅방 목록 가져오기
    @ResponseBody
    @GetMapping("/chat/rooms/user/{userId}")
    public ResponseEntity<List<ChatRoomResponse>> getChatRoomsForUser(@PathVariable int userId) {
        List<ChatRoomJpaEntity> chatRooms = chatRoomService.getChatRoomsForUser(userId);
        List<ChatRoomResponse> response = chatRooms.stream()
            .map(room -> ChatRoomResponse.builder()
                .roomId(room.getRoomId())
                .roomName(room.getRoomName())
                .messages(room.getMessages().stream()
                    .map(message -> ChatMessageResponse.builder()
                        .id(message.getChatMessagesId())
                        .content(message.getMessagesContents())
                        .writer(message.getSender().getUserId())
                        .build())
                    .collect(Collectors.toList()))
                .users(room.getUsers().stream()
                    .map(user -> ChatRoomUserResponse.builder()
                        .roomUserId(user.getUser().getUserId())
                        .nickname(user.getUser().getNickname())
                        .build())
                    .collect(Collectors.toList()))
                .build())
            .collect(Collectors.toList());

        System.out.println("chat room response , " + response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/chat/rooms")
    @ResponseBody
    public ResponseEntity<Map<String, Long>> createChatRoom(@RequestBody Map<String, Object> request) {
        String userNationality = (String) request.get("nationality");
        Integer userId = (Integer) request.get("userId");

        // 상대방의 국적 설정
        String partnerNationality = "Korea".equals(userNationality) ? "Japan" : "Korea";

        // 랜덤한 상대방을 찾는 메소드 호출
        MemberEntity partner = chatRoomService.findRandomUserByNationality(partnerNationality);

        Long roomId = chatRoomService.createChatRoomWithPartner(partner, userId);
        Map<String, Long> response = new HashMap<>();
        response.put("roomId", roomId);
        
        return ResponseEntity.ok(response);
    }

    @ResponseBody
    @PostMapping("/api/chat/rooms/{roomId}/send")
    public ResponseEntity<ChatMessageResponse> sendChatMessage(
        @PathVariable Long roomId,
        @RequestBody ChatMessageRequest chatMessage
    ) {
        ChatMessageResponse response = chatMessageService.sendMessage(roomId, chatMessage);
        return ResponseEntity.ok(response);
    }
    @ResponseBody
    @PostMapping("/chat/rooms/{roomId}")
    public ResponseEntity<Long> enterChatRoom(@PathVariable Long roomId) {
        // 이 부분에 필요한 로직을 추가합니다.
        return ResponseEntity.ok(roomId);
    }

    @ResponseBody
    @GetMapping("/api/chat/rooms/{roomId}")
    public ResponseEntity<ChatRoomResponse> getChatRoomDetails(@PathVariable Long roomId) {
        ChatRoomJpaEntity chatRoom = chatRoomService.loadById(roomId);
        
        ChatRoomResponse response = ChatRoomResponse.builder()
            .roomId(chatRoom.getRoomId())
            .roomName(chatRoom.getRoomName())
            .messages(chatRoom.getMessages().stream()
                .map(message -> ChatMessageResponse.builder()
                    .id(message.getChatMessagesId())
                    .content(message.getMessagesContents())
                    .writer(message.getSender().getUserId())
                    .build())
                .collect(Collectors.toList()))
            .users(chatRoom.getUsers().stream()
                .map(user -> ChatRoomResponse.ChatRoomUserResponse.builder()
                    .roomUserId(user.getUser().getUserId())
                    .nickname(user.getUser().getNickname())
                    .build())
                .collect(Collectors.toList()))
            .build();

        return ResponseEntity.ok(response);
    }
}
