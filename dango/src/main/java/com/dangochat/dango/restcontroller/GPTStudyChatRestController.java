package com.dangochat.dango.restcontroller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.dangochat.dango.dto.GPTChatResponse;
import com.dangochat.dango.dto.GPTRequest;
import com.dangochat.dango.dto.Message;
import com.dangochat.dango.entity.MemberEntity;
import com.dangochat.dango.repository.MemberRepository;
import com.dangochat.dango.security.AuthenticatedUser;
import com.dangochat.dango.service.MemberService;
import com.dangochat.dango.service.StudyService;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/gptChat")
@RequiredArgsConstructor
public class GPTStudyChatRestController {

	@Value("${gpt.model}")
    private String model;

    @Value("${gpt.api.url}")
    private String apiUrl;

    private final RestTemplate  restTemplate;
    private final StudyService  studyService;
    private final MemberService memberservice;
    private final MemberRepository memberRepository;
	
	@PostMapping("gptStudyChat")
    public Map<String, String> gptStudyChatView(@RequestBody Map<String, Integer> payload) {
        Integer userId = payload.get("userId");
        List<String> studyContent = studyService.studyContentForToday(userId);

        List<String> availableContent = new ArrayList<>(studyContent); // 모든 단어를 가져옵니다.

        Map<String, String> response = new HashMap<>();
        if (availableContent.isEmpty()) {
            response.put("studyPrompt", "모든 단어를 학습하셨습니다.");
        } else {
            Random random = new Random();
            String randomStudyContent = availableContent.get(random.nextInt(availableContent.size()));
            response.put("studyPrompt", "해당 어휘를 사용하여 문장을 만들어 GPT에게 전달해 주세요: " + randomStudyContent);
        }

        return response;
    }

    @PostMapping("studyChat")
    public String studyChat(@RequestBody Map<String, Object> payload) throws IOException, MessagingException {
        Integer userId = (Integer) payload.get("userId");
        String userSentence = (String) payload.get("userSentence");
        String studyContent = (String) payload.get("studyContent"); // 프론트에서 받은 학습 내용

        List<Message> userConversationHistory = new ArrayList<>();
        String userNationality = memberservice.getUserNationality(userId);

        if ("Korea".equalsIgnoreCase(userNationality)) {
            userConversationHistory.add(new Message("user", "유저가 '" + studyContent + "' 을 사용해서 문장을 만들거야. 그 문장이 문법적으로나 "
                    + "오타가 없다면 '정답입니다. 새로운 단어로 시도해주세요' 라는 문구를 출력해주고 틀렸다면 틀린 부분을 유저한테 알려줬으면 좋겠어. 유저가 작성한 문장은 '"
                    + userSentence + "' 이야"));
        } else if ("Japan".equalsIgnoreCase(userNationality)) {
            userConversationHistory.add(new Message("user", "ユーザーが「" + studyContent + "」を使って文を作ります。その文が文法的に正しく、"
                    + "誤字がない場合は「正解です。新しい単語で試してください」というメッセージを出力し、間違っている場合はユーザーに間違いを指摘してください。ユーザーが作成した文は「"
                    + userSentence + "」です"));
        }

        GPTRequest request = new GPTRequest(model, userConversationHistory, userSentence, 1, 256, 1, 2, 2);
        GPTChatResponse gptResponse = restTemplate.postForObject(apiUrl, request, GPTChatResponse.class);

        Message gptMessage = gptResponse.getChoices().get(0).getMessage();
        userConversationHistory.add(new Message("assistant", gptMessage.getContent()));

        MemberEntity user = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + userId));
        if (gptMessage.getContent().contains("정답입니다") || gptMessage.getContent().contains("正解です")) {
            user.setUserMileage(user.getUserMileage() + 2);
            memberRepository.save(user);
        }

        return gptMessage.getContent();
    }

}
