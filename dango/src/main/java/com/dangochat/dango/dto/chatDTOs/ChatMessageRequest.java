package com.dangochat.dango.dto.chatDTOs;

public record ChatMessageRequest(int from, int to, String text, Long roomId) {
}