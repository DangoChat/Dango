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

    @Value("${gpt.model}") //gpt-3.5-turbo
    private String model;

    @Value("${gpt.api.url}") //https://api.openai.com/v1/chat/completions
    private String apiUrl;

    private final RestTemplate restTemplate;  // JSON 응답을 Java 객체로 변환하거나, Java 객체를 JSON 형식으로 변환해 API에 보낼 수 있음

    // [승급 테스트] 사용자의 현재 레벨에 맞는 단어를 선택하여 GPT에게 문제를 생성 요청하는 기능
    private List<Message> korCreateMessage(String content, String currentLevel) {
        String questionPrompt = String.format(
                "한국어 능력시험" +currentLevel+ "에 맞게 객관식 문제를 만들어야 합니다. \n" +
                        "정답으로 선택될 단어는 ' "+content+" '라는 단어 이어야 합니다." +
                        "예시 문제:\n" +
                        "{\n" +
                        "  \"content\": \"그는 어제 경찰에 (　　　) 되었다.\",\n" +
                        "  \"options\": [ \"1. 체포\", \"2. 증언\", \"3. 감시\", \"4. 조사\" ],\n" +
                        "  \"answer\": \"1\"\n" +
                        "}\n" +
                        " 이며 그는 어제 경찰에 (　체포　) 되었다 처럼 문장이 어색함없이 잘 구성 되게 해주세요" +
                        "응답은 다음과 같은 정확한 JSON 형식으로 제공되어야 합니다:\n" +
                        "{\n" +
                        "  \"content\": \"문제\",\n" +
                        "  \"options\": [ \"1. 선택지1\", \"2. 선택지2\", \"3. 선택지3\", \"4. 선택지4\" ],\n" +
                        "  \"answer\": \"정답 번호\"\n" +
                        "}\n" +"지정된 형식 이외의 설명이나 추가 정보는 포함하지 마세요."+

                        "중요: 선택지 중 하나만 정답이어야 하며, 그 정답은"+ content+ "이어야 합니다 나머지 세 개의 선택지는 오답이어야합니다.\n" +
                        "문제는 자연스럽고 잘 구성된 문장이어야 하며, 정답은 선택지 중 하나에 배치되어야 하고, \"answer\" 필드에 번호(예: \"1\", \"2\", \"3\", 또는 \"4\")로 지정되어야 합니다.\n" +
                        "\n" +
                        "모든 텍스트, 즉 문제와 선택지는 한국어로 작성되어야 합니다.\n"

        );

        return List.of(new Message("user", questionPrompt));
    }

    // GPT에 문제 요청하고 만들어진 문제들
    public List<String> korGenerateQuestions(List<String> contentList, int messageType, int numOfQuestions, String currentLevel) throws IOException {
        List<String> generatedQuestions = new ArrayList<>();

        // 만들 문제의 개수
        for (int i = 0; i < numOfQuestions; i++) {

            // contentList에서 순차적으로 단어를 가져옴
            String content = contentList.get(i);
            // null이 아니고 빈 공백문자가 아닌지 확인
            if (content != null && !content.trim().isEmpty()) {
                System.out.println("현재 처리 중인 단어: " + content);

                // GPT에게 보낼 메시지
                List<Message> korMessages = korCreateMessage(content, currentLevel);

                // GPT 요청 객체 생성
                GPTRequest request = new GPTRequest(model, korMessages, null, 1, 256, 1, 2, 2);

                // request 객체를 JSON 형식의 문자열로 변환하기 위해
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

        return generatedQuestions;  // 생성된 문제 리스트 반환
    }
}
