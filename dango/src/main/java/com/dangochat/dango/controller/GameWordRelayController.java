package com.dangochat.dango.controller;

import com.dangochat.dango.security.AuthenticatedUser;
import com.dangochat.dango.service.UserMileageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import com.dangochat.dango.dto.GPTChatResponse;
import com.dangochat.dango.dto.GPTRequest;
import com.dangochat.dango.dto.Message;
import lombok.RequiredArgsConstructor;

@Slf4j
@Controller
@RequestMapping("/game/wordRelay")
@RequiredArgsConstructor
public class GameWordRelayController {

    @Value("${gpt.model}")
    private String model;

    @Value("${gpt.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    // 대화 기록을 저장하는 리스트
    private List<Message> conversationHistory = new ArrayList<>();

    private final UserMileageService userMileageService;

    private boolean mileageUpdated = false;  // 마일리지 중복 저장 방지 변수

    // 끝말잇기 게임 시작 페이지
    @GetMapping("/")
    public String showWordRelayGame() {
        return "GameView/wordRelay";  // HTML 페이지 경로를 반환
    }

    // 게임 시작: GPT로부터 첫 번째 단어를 받아옴
    @GetMapping("/start")
    @ResponseBody
    public String startGame() throws IOException {

        // GPT에게 첫 번째 단어 요청
        String prompt = "끝말잇기를 하자. 너는 단어만 보내줘. 설명하지 말고 단어만 보내. 규칙은 다음과 같아," +
                "너가 만약 10초안에 답장을 안보내거나 , 보냈는데 끝말을 못이었거나, 보냈는데 한 글자만 보내거나, 보냈는데 세상에 없는 단어를 보내면 '제가 졌습니다'라고 말해. " +
                "또한, 내가 10초안에 답장을 안보내거나 , 보냈는데 끝말을 못이었거나, 보냈는데 한 글자만 보내거나, 보냈는데 세상에 없는 단어를 보내면 '제가 이겼습니다' 라고 말해. 너가 먼저 시작해";

        // 대화 기록 초기화
        conversationHistory.clear();
        mileageUpdated = false;  // 게임이 새로 시작되면 다시 초기화
        conversationHistory.add(new Message("user", prompt));

        // GPT 요청 생성
        GPTRequest request = new GPTRequest(
                model,
                conversationHistory,
                prompt,
                1, 256, 1, 2, 2
        );

        // GPT 응답 받기
        GPTChatResponse gptResponse = restTemplate.postForObject(apiUrl, request, GPTChatResponse.class);
        Message gptMessage = gptResponse.getChoices().get(0).getMessage();
        conversationHistory.add(new Message("assistant", gptMessage.getContent()));

        log.info("GPT 응답: {}", gptMessage.getContent());

        return gptMessage.getContent().trim();  // 첫 번째 단어 반환
    }

    // 사용자가 입력한 단어를 받아 끝말잇기를 계속함
    @GetMapping("/relay")
    @ResponseBody
    public String relayWord(@RequestParam("word") String word, @AuthenticationPrincipal AuthenticatedUser userDetails) throws IOException {
        log.info("사용자가 단어를 입력함: {}", word);

        // 규칙 적용: 한 글자 단어 또는 끝말을 잇지 못한 경우
        String lastWord = conversationHistory.get(conversationHistory.size() - 1).getContent();

        // 사용자가 한 글자 단어를 입력한 경우
        if (word.length() == 1) {
            return endGame(userDetails, "제가 이겼습니다! (사용자가 한 글자 단어 입력)", conversationHistory.size());
        }

        // 끝말을 잇지 못한 경우
        if (!lastWord.isEmpty() && lastWord.charAt(lastWord.length() - 1) != word.charAt(0)) {
            return endGame(userDetails, "제가 이겼습니다! (끝말을 잇지 못함)", conversationHistory.size());
        }

        // GPT에게 다시 요청하여 끝말잇기를 계속 진행
        conversationHistory.add(new Message("user", word));

        String prompt = "단어만 보내줘. 설명은 필요 없어, 단어만.";
        GPTRequest request = new GPTRequest(
                model,
                conversationHistory,
                prompt,
                1, 256, 1, 2, 2
        );

        GPTChatResponse gptResponse = restTemplate.postForObject(apiUrl, request, GPTChatResponse.class);
        Message gptMessage = gptResponse.getChoices().get(0).getMessage();
        conversationHistory.add(new Message("assistant", gptMessage.getContent()));

        log.info("GPT 응답: {}", gptMessage.getContent());
//
        // GPT가 "제가 졌습니다"라고 응답하면 게임 종료, 500점 추가
        if (gptMessage.getContent().contains("제가 졌습니다")) {
            return endGame(userDetails, "제가 이겼습니다!", conversationHistory.size() + 500);  // 500점 추가
        }

        // GPT가 "제가 이겼습니다"라고 응답하면 게임 종료, 결과 만큼만 마일리지 추가
        if (gptMessage.getContent().contains("제가 이겼습니다")) {
            return endGame(userDetails, "GPT가 이겼습니다.", conversationHistory.size()-1);
        }

        return gptMessage.getContent().trim();  // GPT의 단어 반환
    }

    // 게임 종료 및 마일리지 업데이트 메서드
    private String endGame(AuthenticatedUser userDetails, String message, int score) {
        log.info("게임 종료: {}", message);

        // 마일리지가 이미 업데이트된 상태라면 더 이상 저장하지 않음
        if (!mileageUpdated) {
            log.info("최종 마일리지 점수: {}", score);  // 로그로 마일리지 점수 확인
            updateMileage(userDetails, score);
            mileageUpdated = true;  // 마일리지 업데이트 플래그 설정
        } else {
            log.info("마일리지가 이미 업데이트되었습니다. 중복 저장 방지.");
        }

        return message;
    }

    // 마일리지 업데이트 API
    @PostMapping("/result")
    public ResponseEntity<String> updateMileage(
            @AuthenticationPrincipal AuthenticatedUser userDetails,
            @RequestParam("gameScore") int gameScore) {

        int userId = userDetails.getId();

        try {
            // 마일리지 업데이트
            userMileageService.addMileage(userId, gameScore);
            log.info("마일리지 성공적으로 업데이트됨: userId={}, gameScore={}", userId, gameScore);
            return ResponseEntity.ok("마일리지가 성공적으로 업데이트되었습니다.");
        } catch (Exception e) {
            log.error("마일리지 업데이트 중 오류 발생: userId={}, gameScore={}", userId, gameScore, e);
            return ResponseEntity.status(500).body("마일리지 업데이트 중 오류가 발생했습니다.");
        }
    }
}

