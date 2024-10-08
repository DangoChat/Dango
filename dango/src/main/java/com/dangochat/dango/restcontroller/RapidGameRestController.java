package com.dangochat.dango.restcontroller;

import java.util.Queue;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dangochat.dango.entity.MemberEntity;
import com.dangochat.dango.repository.MemberRepository;
import com.dangochat.dango.service.GameService;
import com.dangochat.dango.service.ChatServices.ChatRoomService;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class RapidGameRestController {

    private final GameService gameService;
    private final ChatRoomService chatRoomService;

    private final Queue<Integer> waitingQueue = new LinkedList<>();
    private final Map<Integer, Long> roomMap = new HashMap<>();

    @PostMapping("/match")
    public synchronized ResponseEntity<Map<String, Object>> matchPlayers(@RequestBody Map<String, Object> payload) {
        int userId = (Integer) payload.get("userId");
        Map<String, Object> response = new HashMap<>();

        // 이미 매칭된 사용자라면 roomId 반환
        if (roomMap.containsKey(userId)) {
            response.put("roomId", roomMap.get(userId));
            return ResponseEntity.ok(response);
        }

        // 대기열에 사용자 추가
        if (!waitingQueue.contains(userId)) {
            waitingQueue.add(userId);
        }

        // 대기열에 2명 이상 있으면 매칭 진행
        if (waitingQueue.size() >= 2) {
            int partnerId = waitingQueue.poll();  // 첫 번째 대기 사용자
            int nextUserId = waitingQueue.poll(); // 두 번째 대기 사용자

            // 방 생성 및 roomId 할당
            Long roomId = chatRoomService.createNewChatRoom("SpeedGameRoom");
            roomMap.put(userId, roomId);
            roomMap.put(partnerId, roomId);
            roomMap.put(nextUserId, roomId);

            response.put("roomId", roomId);
            return ResponseEntity.ok(response);
        } else {
            // 대기 상태로 응답
            response.put("status", "waiting");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
    }
     @PostMapping("/decreaseOpponentHP/{roomId}")
    public ResponseEntity<Void> decreaseOpponentHP(@PathVariable Long roomId) {
        gameService.decreaseOpponentHP(roomId);

        // 게임 종료 조건을 확인하고, 상대방 HP가 0이면 승리 처리
        if (gameService.getOpponentHP(roomId) <= 0) {
            return ResponseEntity.status(HttpStatus.OK).build();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/getHP/{roomId}")
    public ResponseEntity<Map<String, Integer>> getHP(@PathVariable Long roomId) {
        Map<String, Integer> hpStatus = new HashMap<>();
        hpStatus.put("userHP", gameService.getUserHP(roomId));
        hpStatus.put("opponentHP", gameService.getOpponentHP(roomId));
        return ResponseEntity.ok(hpStatus);
    }
}