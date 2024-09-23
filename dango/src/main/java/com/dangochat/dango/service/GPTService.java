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
    public List<GPTResponse> generateQuestions(List<String> studyContent) throws IOException, MessagingException {
    	List<GPTResponse> generatedResponses = new ArrayList<>();  // GPTResponse 객체를 반환하기 위해 리스트 생성

        // studyContent의 각 단어에 대해 개별적으로 GPT에 요청
        for (String studycontent : studyContent) {
            if (studycontent != null && !studycontent.trim().isEmpty()) {
                // 로그 추가: 현재 처리 중인 단어 출력
                System.out.println("Processing content: " + studycontent);

                // 각 단어에 대해 GPT 메시지 생성
                List<Message> messages = List.of(new Message("user","問題１______ の言葉の読み方として最もよいものを、１・２・３・４から一つ選びなさい。\r\n"
                		+ "와 같이 JLPT 형식의 문제를 만들꺼에요. 위에 문제는 유형일 뿐이며, 지금부터 만들어줘야하는 문제는"+"\""+studycontent+"\""+"이 단어를 사용한 예문을 보여주고 단어에 밑줄을 쳐주면 됩니다."
                		+ "문제와 선택지를 모두 일본어로 작성하여 위와 같은 형식의 4지선다 문제를 1문제만 만들어 주세요\r\n"
                		+ "JSON 형식으로 Return을 해주세요.\r\nJSON의 형식은\r\n"
                		+ "{ \"content\": 문제, \"options\": 4지선다 내용, \"answer\" : 정답 } 입니다."));
                
                // GPT 요청 생성
                GPTRequest request = new GPTRequest(model, messages, null, 1, 256, 1, 2, 2);

                // 요청을 JSON 형식으로 직렬화하여 로그로 출력
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    String jsonRequest = objectMapper.writeValueAsString(request);
                    System.out.println("gpt한테 질문한걸 json으로 형식으로 정리해서 보여주는 것: " + jsonRequest);  // 요청 로그 출력
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
                
                // choices 안에서 message의 content를 추출
                if (gptResponse != null && gptResponse.getChoices() != null && !gptResponse.getChoices().isEmpty()) {
                    GPTResponse.Choice choice = gptResponse.getChoices().get(0);  // 첫 번째 choice만 가져옴
                    String messageContent = choice.getMessage().getContent();  // message에서 content 추출

                    // 비정상적인 공백 문자를 일반 공백으로 치환
                    messageContent = messageContent.replace("\u00A0", " ")   // non-breaking space
                    							   .replace("\u3000", " ");  // 전각 공백

                    
                    // messageContent를 다시 JSON으로 파싱하여 content, options, answer 추출
                    ObjectMapper objectMapper2 = new ObjectMapper();
                    try {
                        GPTResponse extractedResponse = objectMapper2.readValue(messageContent, GPTResponse.class);
                        System.out.println("문제 내용: " + extractedResponse.getContent());
                        System.out.println("사지선다 내용: " + extractedResponse.getOptions());
                        System.out.println("정답 번호: " + extractedResponse.getAnswer());

                        generatedResponses.add(extractedResponse);  // 전체 응답을 저장
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return generatedResponses;
    }
}
