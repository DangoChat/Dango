package com.dangochat.dango.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

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
@RequestMapping("gpt")
@RequiredArgsConstructor
public class GPTController {
	
    @Value("${gpt.model}")
    private String model;

    @Value("${gpt.api.url}")
    private String apiUrl;

    private final RestTemplate  restTemplate;
    
    
    // 이전 대화를 저장하는 리스트 (임시로 메모리에 저장)
    private List<Message> conversationHistory = new ArrayList<>();

    
    
    @GetMapping("chat")
    public String chat(@RequestParam("prompt") String prompt) throws IOException, MessagingException {

        // 사용자의 프롬프트를 메시지로 추가
        conversationHistory.add(new Message("user", prompt));

        // GPT 요청 생성 (이전 대화 기록을 포함)
        GPTRequest request = new GPTRequest(
                model,
                conversationHistory,  // 이전 대화 기록을 포함하여 생성
                prompt,
                1, 256, 1, 2, 2
        );
        
        // GPT의 응답을 받기 위해 요청
        GPTChatResponse gptResponse = restTemplate.postForObject(
                apiUrl,
                request,
                GPTChatResponse.class
        );

        // GPT의 응답에서 첫 번째 메시지 추출
        Message gptMessage = gptResponse.getChoices().get(0).getMessage();
        conversationHistory.add(new Message("assistant", gptMessage.getContent()));

        // GPT의 첫 번째 응답을 반환
        return gptMessage.getContent();
    }
    
   

}
