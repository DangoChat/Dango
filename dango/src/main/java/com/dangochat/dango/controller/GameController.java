package com.dangochat.dango.controller;

import com.dangochat.dango.entity.MemberEntity;
import com.dangochat.dango.service.UserMileageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/game")
@RequiredArgsConstructor
public class GameController {

    private final UserMileageService userMileageService;

    @PostMapping("/result")
    public ResponseEntity<String> processGameResult(
            @AuthenticationPrincipal MemberEntity user,
            @RequestParam("gameScore") int gameScore) {

        try {
            // 게임 점수만큼 마일리지 추가
            userMileageService.addMileage(user, gameScore);

            return ResponseEntity.ok("게임 결과가 성공적으로 처리되었습니다. 마일리지: " + gameScore);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("게임 결과 처리 중 오류가 발생했습니다.");
        }
    }
}
