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
	
	
	// 
	@GetMapping("gptStudyChat")
	public Map<String, String> gptStudyChatView(@AuthenticationPrincipal AuthenticatedUser userDetails, HttpSession session) {
	    // 오늘 학습한 내용을 가져옴
	    List<String> studyContent = studyService.studyContentForToday(userDetails.getId());

	    // 이미 사용한 어휘 리스트를 세션에서 가져옴
	    List<String> usedContent = (List<String>) session.getAttribute("usedStudyContent");
	    if (usedContent == null) {
	        usedContent = new ArrayList<>();
	    }

	    // 사용하지 않은 어휘만 필터링 (새로운 리스트 생성)
	    List<String> availableContent = new ArrayList<>();
	    for (String content : studyContent) {
	        if (!usedContent.contains(content)) {
	            availableContent.add(content);
	        }
	    }

	    Map<String, String> response = new HashMap<>();
	    if (availableContent.isEmpty()) {
	        // 모든 단어를 학습한 경우 메시지를 JSON 응답으로 추가
	        response.put("studyPrompt", "모든 단어를 학습하셨습니다.");
	    } else {
	        // 랜덤으로 하나의 학습 내용을 선택
	        Random random = new Random();
	        String randomStudyContent = availableContent.get(random.nextInt(availableContent.size()));

	        // 선택된 어휘를 사용한 어휘 리스트에 추가하고 세션에 저장
	        usedContent.add(randomStudyContent);
	        session.setAttribute("usedStudyContent", usedContent);

	        // 선택된 단어를 세션에 저장하여 나중에 사용
	        session.setAttribute("randomStudyContent", randomStudyContent);

	        // JSON 응답으로 학습 내용 추가
	        response.put("studyPrompt", "해당 어휘를 사용하여 문장을 만들어 GPT에게 전달해 주세요: " + randomStudyContent);
	    }

	    return response;
	}

    // 이전 대화를 저장하는 리스트 (임시로 메모리에 저장)
    private List<Message> conversationHistory = new ArrayList<>();

    
    @GetMapping("studyChat")
    public String studyChat(@RequestParam("userSentence") String userSentence, @AuthenticationPrincipal AuthenticatedUser userDetails, HttpSession session) throws IOException, MessagingException {
        // 세션에서 사용자별 대화 이력을 가져옴
        List<Message> userConversationHistory = (List<Message>) session.getAttribute("conversationHistory");
        if (userConversationHistory == null) {
            userConversationHistory = new ArrayList<>();
            session.setAttribute("conversationHistory", userConversationHistory);
        }

        // 세션에서 저장된 학습 내용을 가져옴
        String randomStudyContent = (String) session.getAttribute("randomStudyContent");
        if (randomStudyContent == null) {
            return "Error: 학습 내용이 없습니다. 다시 시도해 주세요.";
        }

        // 유저의 nationality 정보 가져오기
        String userNationality = memberservice.getUserNationality(userDetails.getId());

        // GPT에 요청할 프롬프트 생성 (사용자의 입력과 학습 내용을 포함하여 GPT에게 질문)
        if ("Korea".equalsIgnoreCase(userNationality)) {
            userConversationHistory.add(new Message("user", "유저가 '" + randomStudyContent + "' 을 사용해서 문장을 만들거야. 그 문장이 문법적으로나 "
                    + "오타가 없다면 '정답입니다. 새로운 단어로 시도해주세요' 라는 문구를 출력해주고 틀렸다면 틀린 부분을 유저한테 알려줬으면 좋겠어. 유저가 작성한 문장은 '"
                    + userSentence + "' 이야"));
        } else if ("Japan".equalsIgnoreCase(userNationality)) {
            userConversationHistory.add(new Message("user", "ユーザーが「" + randomStudyContent + "」を使って文を作ります。その文が文法的に正しく、"
                    + "誤字がない場合は「正解です。新しい単語で試してください」というメッセージを出力し、間違っている場合はユーザーに間違いを指摘してください。ユーザーが作成した文は「"
                    + userSentence + "」です"));
        }

        // GPT에 요청
        GPTRequest request = new GPTRequest(
                model,
                userConversationHistory,
                userSentence,
                1, 256, 1, 2, 2
        );

        GPTChatResponse gptResponse = restTemplate.postForObject(
                apiUrl,
                request,
                GPTChatResponse.class
        );

        Message gptMessage = gptResponse.getChoices().get(0).getMessage();
        userConversationHistory.add(new Message("assistant", gptMessage.getContent()));

        // 사용자의 마일리지 업데이트
        MemberEntity user = memberRepository.findById(userDetails.getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + userDetails.getId()));

        // GPT의 응답이 "정답입니다"인지 확인
        if (gptMessage.getContent().contains("정답입니다")) {
            int updatedMileage = user.getUserMileage() + 2;
            user.setUserMileage(updatedMileage);
            memberRepository.save(user);
        }

        return gptMessage.getContent();
    }

    
    
}
