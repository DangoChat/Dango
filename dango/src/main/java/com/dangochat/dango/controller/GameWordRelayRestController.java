package com.dangochat.dango.controller;

import com.dangochat.dango.security.AuthenticatedUser;
import com.dangochat.dango.service.MemberService;
import com.dangochat.dango.service.UserMileageService;
import com.dangochat.dango.service.UserRankingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RestController  // REST 컨트롤러로 설정
@RequestMapping("/api/game/wordRelay")  // 끝말잇기 게임을 위한 API 엔드포인트 설정
@RequiredArgsConstructor  // 자동으로 의존성 주입을 생성자 주입 방식으로 처리
public class GameWordRelayRestController {

    @Value("${gpt.model}")
    private String model;  // GPT 모델 이름을 가져오는 값

    @Value("${gpt.api.url}")
    private String apiUrl;  // GPT API URL을 가져오는 값

    private final RestTemplate restTemplate;  // 외부 API 통신을 위한 RestTemplate 객체
    private final MemberService memberService;  // 사용자 관련 서비스
    private final UserMileageService userMileageService;  // 마일리지 관련 서비스
    private final UserRankingService userRankingService;  // 사용자 랭킹 관련 서비스

    private List<Message> conversationHistory = new ArrayList<>();  // 대화 기록을 저장하는 리스트
    private boolean mileageUpdated = false;  // 마일리지 업데이트 중복 방지 플래그
    private boolean rankingUpdated = false;  // 랭킹 업데이트 중복 방지 플래그

    // 끝말잇기 게임을 시작하는 엔드포인트
    @PostMapping("/start")
    public ResponseEntity<String> startGame(@AuthenticationPrincipal AuthenticatedUser userDetails) throws IOException {
        int userId = userDetails.getId();  // 인증된 사용자의 ID 가져오기
        String userNationality = memberService.findUserNationalityById(userId);  // 사용자 국적 확인

        // 대화 기록 및 플래그 초기화
        conversationHistory.clear();
        mileageUpdated = false;
        rankingUpdated = false;

        // GPT에게 보낼 첫 번째 요청 메시지 생성
        String prompt = userNationality + "어 단어로 끝말잇기 하자. 너는 " +
                userNationality + "어 단어만 보내줘. 설명은 하지 말고 단어만 보내. 규칙은 다음과 같아." +
                "사전에 등록된 명사만 사용할 수 있고, 한 글자는 사용하면 안돼 그리고 이미 나온 단어는 말하면 안 돼." +
                "만약 내가 규칙을 위반하면 바로 'YOU LOSE'라고 말해줘. 너가 먼저 시작해줘 그리고 너는 사전에 있는 단어를 이용해서 " +
                "내 끝말을 이어줘 내가 규칙을 위반하면 바로 'YOU LOSE'보내줘 ";

        // 대화 기록에 메시지 추가
        conversationHistory.add(new Message("user", prompt));

        // GPT API에 요청 보내기
        GPTRequest request = new GPTRequest(model, conversationHistory, prompt, 1, 256, 1, 2, 2);
        GPTChatResponse gptResponse = restTemplate.postForObject(apiUrl, request, GPTChatResponse.class);

        // GPT 응답 메시지를 대화 기록에 추가
        Message gptMessage = gptResponse.getChoices().get(0).getMessage();
        conversationHistory.add(new Message("assistant", gptMessage.getContent()));

        // GPT의 첫 번째 단어를 반환
        return ResponseEntity.ok(gptMessage.getContent().trim());
    }

    // 사용자의 단어를 GPT에 전달하고 응답을 받는 엔드포인트
    @PostMapping("/relay")
    public ResponseEntity<String> relayWord(@RequestParam("word") String word, @AuthenticationPrincipal AuthenticatedUser userDetails) throws IOException {
        conversationHistory.add(new Message("user", word));  // 사용자의 단어를 대화 기록에 추가

        // GPT에게 보낼 요청 생성
        String prompt = "단어만 보내줘. 설명은 필요 없어, 단어만.";
        GPTRequest request = new GPTRequest(model, conversationHistory, prompt, 1, 256, 1, 2, 2);

        // GPT API에 요청 보내기
        GPTChatResponse gptResponse = restTemplate.postForObject(apiUrl, request, GPTChatResponse.class);
        Message gptMessage = gptResponse.getChoices().get(0).getMessage();
        conversationHistory.add(new Message("assistant", gptMessage.getContent()));  // GPT의 응답을 대화 기록에 추가

        // 'YOU LOSE' 또는 'YOU WIN' 메시지가 포함된 경우 대화 기록을 초기화하고 게임을 종료
        if (gptMessage.getContent().contains("YOU LOSE") || gptMessage.getContent().contains("YOU WIN")) {
            conversationHistory.clear();  // 게임 종료 후 대화 기록 초기화
            return ResponseEntity.ok(gptMessage.getContent());
        }

        return ResponseEntity.ok(gptMessage.getContent());  // GPT의 단어 응답 반환
    }

    // 게임 결과를 서버에 전송하여 마일리지 및 랭킹을 업데이트하는 엔드포인트
    @PostMapping("/result")
    public ResponseEntity<String> updateMileageAndRanking(
            @AuthenticationPrincipal AuthenticatedUser userDetails,
            @RequestParam("gameScore") int gameScore) {

        int userId = userDetails.getId();  // 로그인된 사용자 ID 가져오기

        try {
            // 마일리지 업데이트 (중복 방지 처리)
            if (!mileageUpdated) {
                userMileageService.addMileage(userId, gameScore);  // 마일리지 추가
                mileageUpdated = true;  // 마일리지 업데이트 플래그 설정
            }

            // 랭킹 업데이트 (중복 방지 처리)
            if (!rankingUpdated) {
                userRankingService.updateUserRanking(userId, gameScore);  // 사용자 랭킹 업데이트
                rankingUpdated = true;  // 랭킹 업데이트 플래그 설정
            }

            conversationHistory.clear();  // 게임 종료 후 대화 기록 초기화

            return ResponseEntity.ok("마일리지와 랭킹이 성공적으로 업데이트되었습니다.");  // 성공 응답 반환
        } catch (Exception e) {
            return ResponseEntity.status(500).body("업데이트 중 오류가 발생했습니다.");  // 오류 발생 시 응답 반환
        }
    }
}
