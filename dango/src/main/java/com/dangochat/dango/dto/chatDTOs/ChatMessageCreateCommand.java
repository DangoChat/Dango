package com.dangochat.dango.dto.chatDTOs;

import lombok.Builder;

@Builder
public record ChatMessageCreateCommand(Long roomId, String content, int to, int from) {
}
