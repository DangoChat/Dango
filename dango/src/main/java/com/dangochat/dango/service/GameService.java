package com.dangochat.dango.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.dangochat.dango.service.ChatServices.ChatRoomService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GameService {
    
    public boolean checkAnswer(String correctPronunciation, String userAnswer) {
        return correctPronunciation.equalsIgnoreCase(userAnswer.trim());
    }

    private final Map<Long, Integer> userHP = new HashMap<>(); // 각 사용자 HP를 관리
    private final Map<Long, Integer> opponentHP = new HashMap<>(); // 상대방 HP를 관리

    // 게임 시작 시 HP 초기화
    public void initializeHP(Long roomId) {
        userHP.put(roomId, 5);      // 초기 HP를 5로 설정
        opponentHP.put(roomId, 5);  // 상대방 HP를 5로 설정
    }

    // HP 감소 메서드
    public void decreaseOpponentHP(Long roomId) {
        int currentHP = opponentHP.getOrDefault(roomId, 5);
        currentHP -= 1;
        opponentHP.put(roomId, currentHP);
    }

    // 현재 HP 상태 가져오기
    public int getOpponentHP(Long roomId) {
        return opponentHP.getOrDefault(roomId, 5); // 기본값 5
    }

    public int getUserHP(Long roomId) {
        return userHP.getOrDefault(roomId, 5); // 기본값 5
    }
}
