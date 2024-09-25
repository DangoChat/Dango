package com.dangochat.dango.service;


import com.dangochat.dango.dto.GPTRequest;
import com.dangochat.dango.dto.GPTResponse;
import com.dangochat.dango.dto.Message;
import com.dangochat.dango.dto.StudyDTO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

/**
 * GPT를 활용한 문법 문제를 만드는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GPTGrammerService {


    @Value("${gpt.model}") //gpt-3.5-turbo
    private String model;

    @Value("${gpt.api.url}") //https://api.openai.com/v1/chat/completions
    private String apiUrl;

    private final RestTemplate restTemplate;

    // GPT 문법 프롬프트 메시지를 생성하는 메서드
    private List<Message> createGrammerMessages(StudyDTO content, int promptType) {
        String prompt;
        switch (promptType) {
            case 1:
                prompt = content.getContent() + "는 " + content.getMeaning() +
                "라는 뜻의 문법입니다." +
                content.getContent() + "을 사용하여 빈칸 채우기 문제를 만들어 주세요. "
                + "예를 들어, \"水を (　　　) にしないようにね。\"와 같은 형식의 문제가 필요합니다. "
                + "빈칸에 " + content.getContent() + "의 알맞은 형태가 무엇인지를 객관식 보기에서 고르는 문제입니다."
                + "객관식 보기는 4개 제공해 주세요. 예시는 1. 出しっぱなし  2. 出しつづけ  3. 出しながし  4. 出しかけ 와 같이 작성해 주세요. "
                + "예시로 주어진 문장과 보기는 사용하지 말고, 새로운 문제와 보기를 작성하세요. "
                + "문제에는 반드시 빈칸이 있어야합니다."
                + "문제에는 문맥상 자연스럽고 문법적으로 적절한 문장이 포함되어야 합니다. "
                + "그리고, 보기의 내용도 문맥상 자연스럽고 문법적으로 적절해야합니다."
                + "문법 사용이 부자연스럽거나, 일상적인 대화에서 사용되지 않는 표현은 피해주세요. "
                + "전부 반드시 일본어로 작성해 주세요. "
                + "답안은 'answer' 필드에 1에서 4 사이의 숫자로 표시해 주세요. "
                + "문제의 빈칸에 답안을 넣었을 때, 문맥상으로 적절한 문장이어야 합니다."
                + "그리고, 문제의 빈칸에 답안을 넣었을 때, 자연스러운 문장이어야합니다."
                + "반드시 일본어 문장으로만 작성해 주세요. 문제 형식은 다음과 같이 JSON 형식으로 만들어 주세요: "
                + "{\"content\": \"문제\", \"options\": [\"1. 보기1\", \"2. 보기2\", \"3. 보기3\", \"4. 보기4\"], \"answer\": \"정답 번호\"}. "
                + "문제는 반드시 일본어 문법 문제로 작성해 주세요. 일반 상식 문제는 포함하지 마세요.";
                break;
        
            default:
                throw new IllegalArgumentException("잘못된 메시지 유형입니다.");
        }

        return List.of(new Message("user", prompt));
    }

    // GPT에 생성한 메시지로 질문 응답 받고 리턴
    public List<String> generateGrammerQuestions(List<StudyDTO> contentList, int messageType, int numOfQuestions) throws IOException {
        List<String> generatedQuestions = new ArrayList<>();

        // 만들 문제의 개수
        for( int i = 0 ; i < numOfQuestions ; i++ ) {
            
            // contentList의 문법 리스트를 순사적으로 가져옴
            StudyDTO content = contentList.get(i);
            // null 이 아니고 빈 공백문자가 아닌지 확인
            if (content != null && !content.getContent().trim().isEmpty()){
                log.debug("현재 처리 중인 문법 : {}, 뜻 : {}", content.getContent(), content.getMeaning());
                // gpt 에게 보낼 메시지 
                List<Message> messages = createGrammerMessages(content, messageType);

                // gpt 요청 객체 생성
                GPTRequest request = new GPTRequest(model, messages, null,0.7, 256, 1, 0, 0);

                // request 객체를 JSON 형식의 문자열로 변환하기 위한 객체
                ObjectMapper objectMapper = new ObjectMapper();
                String jsonRequest;
                try{
                    jsonRequest = objectMapper.writeValueAsString(request);
                    // log.debug("GPT 에 보낼 요청 : {}", jsonRequest);
                } catch ( JsonProcessingException e ){
                    e.printStackTrace();
                    throw new IOException("JSON 변환 중 오류 발생 ", e);
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
        return generatedQuestions;
    }

    public void loadQuestions(HttpSession session, int startIndex, int count) {
        List<StudyDTO> contentList = (List<StudyDTO>) session.getAttribute("grammerList");
        List<String> grammerQuestions = new ArrayList<>();

        try{
            int messageType = (int) session.getAttribute("messageType");
            grammerQuestions = generateGrammerQuestions(contentList, messageType, count);
            session.setAttribute("grammerQuestions", grammerQuestions);
            log.debug("초기 문제 : {}", grammerQuestions);
        } catch (Exception e ) {
            log.error("문제 생성 X", e);
        }
    }
}
