package com.dangochat.dango.service;

import com.dangochat.dango.dto.GPTRequest;
import com.dangochat.dango.dto.GPTResponse;
import com.dangochat.dango.dto.Message;
import com.dangochat.dango.repository.StudyRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 * JLPT 문제 생성 및 관리 서비스
 */

@Service
@RequiredArgsConstructor
public class JLPTLevelupTestService {

    private final StudyRepository studyRepository;

    @Value("${gpt.model}") //gpt-3.5-turbo
    private String model;

    @Value("${gpt.api.url}") //https://api.openai.com/v1/chat/completions
    private String apiUrl;

    private final RestTemplate restTemplate;

    // JLPT 레벨에 맞는 문제 생성 요청 메시지 생성
    private List<Message> createJLPTMessage(String content, String currentLevel) {
        String questionPrompt = String.format(
                "You must create a JLPT " + currentLevel + " level multiple-choice question where the correct word is chosen to fill the blank '(　　　)'. "
                        + "The sentence must be grammatically correct and meaningful in Japanese, and it should be a well-formed sentence that is appropriate for the JLPT " + currentLevel + " level."
                        + "The response must be in the following exact JSON format: "
                        + "{ \"content\": \"question\", \"options\": [ \"1. option1\", \"2. option2\", \"3. option3\", \"4. option4\" ], \"answer\": \"correct_option_number\" }"
                        + "Example question: { \"content\": \"彼は昨日詐欺罪で (　　　) されました。\", \"options\": [ \"1. 訴訟\", \"2. 告発\", \"3. 調査\", \"4. 逮捕\" ], \"answer\": \"4\" }"
                        + "Important: Only one of the options must be the correct answer, which is \"" + content + "\". The remaining three options must be incorrect but plausible distractors."
                        + "Ensure the question is a natural and well-formed sentence, and the correct answer is placed in one of the options, assigned to the \"answer\" field as a single number (e.g., \"1\", \"2\", \"3\", or \"4\")."
                        + "All text, including the question and options, must be written entirely in Japanese."
                        + "The response must strictly follow the provided JSON format. Do not include any explanations, comments, or additional information outside of the specified format."
        );

        return List.of(new Message("user", questionPrompt));
    }

    // GPT에 문제 요청하고 만들어진 문제들 가져오기
    public List<String> generateJLPTQuestions(List<String> contentList, int numOfQuestions, String currentLevel) throws IOException {
        List<String> generatedQuestions = new ArrayList<>();

        for (int i = 0; i < numOfQuestions; i++) {
            String content = contentList.get(i);
            if (content != null && !content.trim().isEmpty()) {
                System.out.println("현재 처리 중인 단어: " + content);

                List<Message> messages = createJLPTMessage(content, currentLevel);

                GPTRequest request = new GPTRequest(model, messages, null, 1, 256, 1, 2, 2);

                ObjectMapper objectMapper = new ObjectMapper();
                String jsonRequest;
                try {
                    jsonRequest = objectMapper.writeValueAsString(request);
                    System.out.println("GPT에 보낼 요청: " + jsonRequest);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    throw new IOException("JSON 변환 중 오류 발생", e);
                }

                GPTResponse gptResponse;
                try {
                    gptResponse = restTemplate.postForObject(
                            apiUrl,
                            request,
                            GPTResponse.class
                    );

                    if (gptResponse != null && gptResponse.getChoices() != null) {
                        String quizResponse = gptResponse.getChoices().get(0).getMessage().getContent();
                        generatedQuestions.add(quizResponse);
                    } else {
                        System.err.println("문제가 생성되지 않았습니다.");
                    }
                } catch (Exception e) {
                    System.err.println("GPT API 통신 중 오류 발생: " + e.getMessage());
                    throw new IOException("GPT API 통신 중 오류 발생", e);
                }
            }
        }

        return generatedQuestions;  // 생성된 질문 리스트 반환
    }
}
