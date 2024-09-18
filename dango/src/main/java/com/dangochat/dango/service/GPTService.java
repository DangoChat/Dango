package com.dangochat.dango.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.dangochat.dango.dto.GPTRequest;
import com.dangochat.dango.dto.GPTResponse;
import com.dangochat.dango.dto.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GPTService {

    @Value("${gpt.model}")
    private String model;

    @Value("${gpt.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    // 파라미터를 List<String>으로 변경
    public List<String> generateQuestions(List<String> studyContent) throws IOException, MessagingException {
        List<String> generatedQuestions = new ArrayList<>();

        // studyContent의 각 단어에 대해 개별적으로 GPT에 요청
        for (String content : studyContent) {
            if (content != null && !content.trim().isEmpty()) {
                // 로그 추가: 현재 처리 중인 단어 출력
                System.out.println("Processing content: " + content);

                // 각 단어에 대해 GPT 메시지 생성
                List<Message> messages = List.of(new Message("user","JLPT 수준에서"+content+"이라는 단어를 사용하는 4지선다 "
                		+ "청해문제를 만들어 주세요. 문제는 모두 일본어로 작성되어야 하고 問題：(문제) 이런식으로 내주세요,문제도 정답도 일본어로 내주세요."
                		+ "그리고 정답알려줄땐 정답만 알려주세요 예를들어 正解：3 이런식으로"));

                // GPT 요청 생성
                GPTRequest request = new GPTRequest(model, messages, null, 1, 256, 1, 2, 2);

                // 요청을 JSON 형식으로 직렬화하여 로그로 출력
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    String jsonRequest = objectMapper.writeValueAsString(request);
                    System.out.println("Serialized JSON Request: " + jsonRequest);  // 요청 로그 출력
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                GPTResponse gptResponse;
                try {
                    gptResponse = restTemplate.postForObject(apiUrl, request, GPTResponse.class);
                    System.out.println("Received response: " + gptResponse);  // 응답 로그 출력
                } catch (Exception e) {
                    System.err.println("Error during GPT API communication: " + e.getMessage());
                    e.printStackTrace();
                    throw new IOException("Error occurred while communicating with GPT API", e);
                }

                // GPT 응답이 유효한지 확인 후 질문 수집
                if (gptResponse != null && gptResponse.getChoices() != null && !gptResponse.getChoices().isEmpty()) {
                    String generatedQuestion = gptResponse.getChoices().get(0).getMessage().getContent();
                    System.out.println("Generated question: " + generatedQuestion);  // 생성된 질문 로그 출력
                    generatedQuestions.add(generatedQuestion);
                }
            }
        }

        // 최종적으로 생성된 질문 리스트 출력
        System.out.println("Final generated questions: " + generatedQuestions);

        return generatedQuestions;
    }
}
