package com.dangochat.dango.dto.chatDTOs;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomResponse {
    private Long roomId;
    private String roomName;
    private List<ChatMessageResponse> messages; // 최근 메시지 등의 정보 포함 가능
    private List<ChatRoomUserResponse> users; // ChatRoomUserResponse로 각 유저의 정보를 담은 리스트

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatRoomUserResponse {
        private int roomUserId;
        private String nickname; // 유저의 닉네임
    }
}
