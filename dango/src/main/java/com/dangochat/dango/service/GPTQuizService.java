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
 * GPT로 단어문제 만들기
 */

@Service
@RequiredArgsConstructor
public class GPTQuizService {
    private final StudyRepository studyRepository;

    @Value("${gpt.model}") //gpt-3.5-turbo
    private String model;

    @Value("${gpt.api.url}") //https://api.openai.com/v1/chat/completions
    private String apiUrl;

    private final RestTemplate restTemplate;  //JSON 응답을 Java 객체로 변환하거나, Java 객체를 JSON 형식으로 변환해 API에 보낼 수 있음

    // GPT 요청 메시지를 생성하는 메서드
    private List<Message> createMessage(String content, String currentLevel) {
        String questionPrompt = String.format(
                "You must create a JLPT " + currentLevel + " level multiple-choice question where the correct word is chosen to fill the blank '(　　　)'. "
                        + "The sentence must be grammatically correct and meaningful in Japanese, and it should be a well-formed sentence that is appropriate for the JLPT " + currentLevel + " level."
                        + "The response must be in the following exact JSON format: "
                        + "{ \"content\": \"question\", \"options\": [ \"1. option1\", \"2. option2\", \"3. option3\", \"4. option4\" ], \"answer\": \"correct_option_number\" }"
                        + "Example question: { \"content\": \"彼は昨日詐欺罪で (　　　) されました。\", \"options\": [ \"1. 訴訟\", \"2. 告発\", \"3. 調査\", \"4. 逮捕\" ], \"answer\": \"4\" }"
                        + "Important: Only one of the options must be the correct answer, which is \"" + content + "\". The remaining three options must be incorrect but plausible distractors."
                        + "Ensure the question is a natural and well-formed sentence, and the correct answer is placed in one of the options, assigned to the \"answer\" field as a single number (e.g., \"1\", \"2\", \"3\", or \"4\")."
                        + "All text, including the question and options, must be written entirely in Japanese."
                        + "The response must strictly follow the provided JSON format. Do not include any explanations, comments, or additional information outside of the specified format.");


        return List.of(new Message("user", questionPrompt));
    }


    // GPT에 문제 요청하고 응답 받는 메서드
    public List<String> generateQuestions(List<String> contentList, int messageType, int numOfQuestions, String currentLevel) throws IOException {
        List<String> generatedQuestions = new ArrayList<>();

        // 만들 문제의 개수
        for (int i = 0; i < numOfQuestions; i++) {

            // contentList에서 순차적으로 단어를 가져옴
            String content = contentList.get(i);
            //null이 아니고 빈 공백문자가 아닌지 확인
            if (content != null && !content.trim().isEmpty()) {
                System.out.println("현재 처리 중인 단어: " + content);

                // gpt에게 보낼 메시지 (단어와 문제유형을 messages에 담아서 보냄)
                List<Message> messages = createMessage(content, currentLevel);

                // GPT 요청 객체 생성(3.5 gpt 와, messages, 등을 보냄)
                GPTRequest request = new GPTRequest(model, messages, null,1, 256, 1, 2, 2);

                // request 객체를 JSON 형식의 문자열로 변환하기 위해
                ObjectMapper objectMapper = new ObjectMapper();
                String jsonRequest;
                try {
                    jsonRequest = objectMapper.writeValueAsString(request);
                    System.out.println("GPT에 보낼 요청: " + jsonRequest);  // 요청 내용을 출력해 확인
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    throw new IOException("JSON 변환 중 오류 발생", e);
                }

                // GPT API 호출 및 응답 처리
                GPTResponse gptResponse;
                try {
                    gptResponse = restTemplate.postForObject(
                            apiUrl,   // 요청을 보낼 API의 URL
                            request,  // POST 요청의 본문에 포함될 데이터 (GPTRequest 객체)
                            GPTResponse.class // 응답을 변환할 클래스 타입 (GPTResponse 객체)
                    );

                    // GPT 응답에서 문제 파싱
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