package com.dangochat.dango.controller;

import com.dangochat.dango.security.AuthenticatedUser;
import com.dangochat.dango.service.MemberService;
import com.dangochat.dango.service.UserMileageService;
import com.dangochat.dango.service.UserRankingService;
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
    private final MemberService memberService;
    private final UserMileageService userMileageService;
    private final UserRankingService userRankingService;

    private List<Message> conversationHistory = new ArrayList<>();
    private boolean mileageUpdated = false;
    private boolean rankingUpdated = false;  // 랭킹 업데이트 중복 방지 변수 추가

    @GetMapping("/")
    public String showWordRelayGame() {
        return "GameView/wordRelay";
    }

    @GetMapping("/start")
    @ResponseBody
    public String startGame(@AuthenticationPrincipal AuthenticatedUser userDetails) throws IOException {
        int userId = userDetails.getId();
        String userNationality = memberService.findUserNationalityById(userId);

        conversationHistory.clear();
        mileageUpdated = false;  // 마일리지 업데이트 중복 방지 초기화
        rankingUpdated = false;  // 랭킹 업데이트 중복 방지 초기화

        //대소문자 모두 동일하게 처리해서 국적이 japan이면 korea language 로 끝말잇기 할 수 있게 만듬
        if (userNationality.equalsIgnoreCase("korea")) {
            userNationality = "japan";
        } else if (userNationality.equalsIgnoreCase("japan")) {
            userNationality = "korea";
        }
        String prompt = userNationality + " 언어로 끝말잇기 게임을 시작하자. 너는 " +
                userNationality + " 단어만 보내줘. 설명하지 말고, 단어만 보내. 규칙은 다음과 같아. " +
                "네이버에서 검색 가능한 명사만 사용할 수 있고, 한 글자 단어는 사용할 수 없어. 이미 사용된 단어를 반복해서도 안 돼. " +
                "또한, 단어가 'ん'으로 끝나면 그 앞의 음절을 사용해서 이어갈 수 있어 (예: 'しんぶん'이면 'ぶん'). " +
                "만약 내가 규칙을 어기면 바로 'YOU LOSE'라고 말해줘. 너가 먼저 시작해줘. 너는 네이버에서 검색할 수 있는 단어를 사용해서 " +
                "내 마지막 단어를 이어줘. 내가 규칙을 어기면 'YOU LOSE'라고 보내줘.";


        log.info("Generated prompt: {}", prompt);

        conversationHistory.add(new Message("user", prompt));

        GPTRequest request = new GPTRequest(model, conversationHistory, prompt, 1, 256, 1, 2, 2);
        GPTChatResponse gptResponse = restTemplate.postForObject(apiUrl, request, GPTChatResponse.class);

        Message gptMessage = gptResponse.getChoices().get(0).getMessage();
        conversationHistory.add(new Message("assistant", gptMessage.getContent()));

        return gptMessage.getContent().trim();  // 첫 번째 단어 반환
    }


    @GetMapping("/relay")
    @ResponseBody
    public String relayWord(@RequestParam("word") String word, @AuthenticationPrincipal AuthenticatedUser userDetails) throws IOException {
        conversationHistory.add(new Message("user", word));

        String prompt = "단어만 보내줘. 설명은 필요 없어, 단어만.";
        GPTRequest request = new GPTRequest(model, conversationHistory, prompt, 1, 256, 1, 2, 2);

        GPTChatResponse gptResponse = restTemplate.postForObject(apiUrl, request, GPTChatResponse.class);
        Message gptMessage = gptResponse.getChoices().get(0).getMessage();
        conversationHistory.add(new Message("assistant", gptMessage.getContent()));

        if (gptMessage.getContent().contains("YOU LOSE") || gptMessage.getContent().contains("YOU WIN")) {
            conversationHistory.clear();  // 게임 종료 후 대화 기록 초기화
            return gptMessage.getContent();
        }

        return gptMessage.getContent();
    }

    @PostMapping("/result")
    public ResponseEntity<String> updateMileageAndRanking(
            @AuthenticationPrincipal AuthenticatedUser userDetails,
            @RequestParam("gameScore") int gameScore) {

        int userId = userDetails.getId();  // 로그인된 사용자 ID

        try {
            if (!mileageUpdated) {
                userMileageService.addMileage(userId, gameScore);
                mileageUpdated = true;
            }

            if (!rankingUpdated) {
                // 랭킹 업데이트
                userRankingService.updateUserRanking(userId, gameScore);
                rankingUpdated = true;
            }

            conversationHistory.clear();  // 게임 종료 후 대화 기록 초기화

            return ResponseEntity.ok("마일리지와 랭킹이 성공적으로 업데이트되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("업데이트 중 오류가 발생했습니다.");
        }
    }
}