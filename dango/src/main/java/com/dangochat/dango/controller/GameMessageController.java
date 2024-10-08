package com.dangochat.dango.controller;

import java.util.List;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.dangochat.dango.dto.StudyDTO;
import com.dangochat.dango.entity.StudyEntity;
import com.dangochat.dango.service.StudyService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class GameMessageController {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final StudyService studyService;

    @MessageMapping("/game/{roomId}/word")
    public void sendGameWord(@DestinationVariable Long roomId) {
        List<StudyEntity> words = studyService.getRandomStudyContentByLevelAndType("N3", "단어", 0); // userId는 필요 시 추가
        if (!words.isEmpty()) {
            StudyEntity word = words.get(0); // 임시로 첫 단어만 사용
            messagingTemplate.convertAndSend("/topic/game/" + roomId, word);
        }
    }
}
