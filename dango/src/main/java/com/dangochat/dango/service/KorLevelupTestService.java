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
public class KorLevelupTestService {
    private final StudyRepository studyRepository;

    @Value("${gpt.model}")
    private String model;

    @Value("${gpt.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    // GPT에 보낼 메시지 생성
    private List<Message> korCreateMessage(String content, String currentLevel) {
        String questionPrompt = String.format(
                "한국어능력시험 스타일의 객관식 문제를 1개만 생성해주세요." +
                        "한국어능력시험 " + currentLevel + " 수준으로 내주세요." +
                        "먼저 ()에 들어갈 말이 " + content + "이어야 합니다. " +
                        "이 " + content + "을 넣었을 때 문장이 문법적으로나 문맥상으로 완벽해야 합니다." +
                        "이 문제는 ()가 있는 형식으로, ()에 들어갈 알맞은 정답을 선택하는 문제입니다. 정답은 반드시 " + content + "이어야 합니다." +
                        "2. 각 문제에는 네 개의 선택지가 포함되어야 합니다. 그 중 하나는 " + content + "이며, 이것이 정답입니다. " +
                        "나머지 세 가지 오답은 문맥상 매우 어색한 단어들이어야 합니다. " +
                        "즉, 오답들이 ()에 들어갔을 때 문장이 어색하고 맞지 않아야 합니다." +
                        "3. 아래와 같은 JSON 형식으로 출력해주세요:\n" +
                        "{ \"content\": \"문장\", \"options\": [ \"1. 오답1\", \"2. 오답2\", \"3. 정답\", \"4. 오답3\" ], \"answer\": \"정답의 번호\" }\n" +
                        "answer는 정답의 번호를 숫자로만 표기해주세요. 그리고 지정된 형식 이외의 설명이나 추가 정보는 포함하지 마세요."
        );
        return List.of(new Message("user", questionPrompt));
    }

    // GPT에 문제 요청하고 만들어진 문제들을 반환하는 메서드
    public List<String> korGenerateQuestions(List<String> contentList, int messageType, int numOfQuestions, String currentLevel) throws IOException {
        List<String> generatedQuestions = new ArrayList<>();

        // numOfQuestions 만큼 반복하여 contentList에서 순차적으로 단어를 가져옴
        for (int i = 0; i < numOfQuestions; i++) {
            String content = contentList.get(i);  // contentList의 i번째 단어를 사용
            if (content != null && !content.trim().isEmpty()) {
                System.out.println("현재 처리 중인 단어: " + content);

                // GPT에게 보낼 메시지 생성
                List<Message> korMessages = korCreateMessage(content, currentLevel);

                // GPT 요청 객체 생성
                GPTRequest request = new GPTRequest(model, korMessages, null, 1, 256, 1, 2, 2);

                ObjectMapper objectMapper = new ObjectMapper();
                String jsonRequest;
                try {
                    jsonRequest = objectMapper.writeValueAsString(request);
                    System.out.println("GPT에 보낼 요청: " + jsonRequest);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    throw new IOException("JSON 변환 중 오류 발생", e);
                }

                // GPT API 호출 및 응답 처리
                GPTResponse gptResponse;
                try {
                    gptResponse = restTemplate.postForObject(
                            apiUrl,
                            request,
                            GPTResponse.class
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

        return generatedQuestions;
    }
}
