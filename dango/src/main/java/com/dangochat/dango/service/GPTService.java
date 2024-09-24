package com.dangochat.dango.service;

import com.dangochat.dango.dto.GPTRequest;
import com.dangochat.dango.dto.GPTResponse;
import com.dangochat.dango.dto.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GPTService {

    @Value("${gpt.model}") // GPT 모델 지정
    private String model;

    @Value("${gpt.api.url}") // GPT API URL 지정
    private String apiUrl;

    private final RestTemplate restTemplate;

    // GPT에 문제 요청하고 응답 받는 메서드
 // GPT에 문제 요청하고 응답 받는 메서드 (매개변수를 List<String>으로 수정)
    public List<String> generateQuestions2(List<String> contentList) throws IOException {
        List<String> generatedQuestions = new ArrayList<>();

        for (String content : contentList) {
            if (content != null && !content.trim().isEmpty()) {
                // GPT에 보낼 메시지 생성
                List<Message> messages = createMessages2(content, 1); // messageType을 고정

                // GPT 요청 객체 생성
                GPTRequest request = new GPTRequest(model, messages, null, 1, 256, 1, 2, 2);
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    String jsonRequest = objectMapper.writeValueAsString(request);
                    System.out.println("GPT 요청: " + jsonRequest);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                GPTResponse gptResponse;
                try {
                    gptResponse = restTemplate.postForObject(apiUrl, request, GPTResponse.class);
                } catch (Exception e) {
                    throw new IOException("GPT API 통신 중 오류 발생", e);
                }

                if (gptResponse != null && gptResponse.getChoices() != null) {
                    String quizResponse = gptResponse.getChoices().get(0).getMessage().getContent();
                    generatedQuestions.add(quizResponse);
                }
            }
        }
        return generatedQuestions;  
    }


    // GPT 메시지를 생성하는 메서드
    private List<Message> createMessages2(String content, int promptType) {
        String prompt;
        switch (promptType) {
        case 1:
            prompt =
                    content  +
                            "이 단어를 사용한 예문을 일본어로 하나 만들어 주세요. 이 단어에만 <u>밑줄</u>을 쳐주고, "
                            + "이 단어의 읽는 방법(일본어 요미가나로)을 객관식 4지선다로 보여주세요. "
                            + "JSON 형식으로 다음과 같이 반환해 주세요: "
                            + "{ \"content\": \"문제\", \"options\": [\"1번\", \"2번\", \"3번\", \"4번\"], \"answer\": \"정답인것의 숫자만 표시\" } "
                            + "반드시 줄바꿈해서 보기 쉽게 보여주세요."
                            + " **일본어로만 문제와 객관식을 만들어 주세요** "
                            + "응답에 한국어나 영어 또는 이상한 문자는 절대 포함되지 않도록 주의해 주세요.";
            break;

        case 2:
            prompt =  content + "이라는 단어를 사용한 **새로운** 문제를 만들어 주세요. "
                    + "이 단어는 문제 문장이나 객관식 보기에 들어갈 수 있으며, 정답이 아니어도 괜찮습니다. "
                    + "JLPT 스타일로 (　　　)부분에 들어갈 단어를 고르는 4지선다 문제를 만들어 주세요. "
                    + "문제 예시: \"今回の飛行機事故のそもそもの (　　　) はまだ分かっていません。\" "
                    + "객관식 예시: 1. 成因  2. 原因  3. 起因  4. 因果 "
                    + "이 형식으로 괄호 `(　　　)`를 사용한 문제를 만들어 주세요. "
                    + "JSON 형식으로 다음과 같이 반환해 주세요: "
                    + "{ \"content\": \"문제\", \"options\": [\"1번\", \"2번\", \"3번\", \"4번\"], \"answer\": \"정답인것의 숫자만 표시\" } "
                    + "반드시 줄바꿈해서 보기 쉽게 보여주세요. "
                    + "괄호 `(　　　)`를 사용하고, 문제와 객관식은 일본어로만 작성해 주세요.** "
                    + "응답에 한국어나 영어가 절대 포함되지 않도록 주의해 주세요.";
            break;


        case 3:
            prompt =
                    content + "이라는 단어가 포함된 **새로운** 문제를 만들어 주세요. "
                            + "이 단어는 문제 문장이나 객관식 보기에 들어갈 수 있으며, 정답이 아니어도 괜찮습니다. "
                            + "밑줄의 단어의 의미와 가장 가까운 것을 고르는 JLPT 스타일의 4지선다 문제를 만들어 주세요. "
                            + "문제 예시: \"子供向けの絵本に<u>ややこしい</u>説明はない。\" "
                            + "객관식 예시: 1. 奇妙な  2. 複雑な  3. 簡潔な  4. 明確な "
                            + "이 형식으로 문제를 만들어 주세요. "
                            + "JSON 형식으로 다음과 같이 반환해 주세요: "
                            + "{ \"content\": \"문제\", \"options\": [\"1번\", \"2번\", \"3번\", \"4번\"], \"answer\": \"정답인것의 숫자만 표시\" } "
                            + "반드시 줄바꿈해서 보기 쉽게 보여주세요."
                            + " **일본어로만 문제와 객관식을 만들어 주세요** "
                            + "응답에 한국어나 영어 또는 이상한 문자는 절대 포함되지 않도록 주의해 주세요.";

            ;
            break;

        case 4:
            prompt =
                    content + "이라는 단어를 포함한 **새로운** 문제를 만들어 주세요. "
                            + "이 단어는 문제 문장이나 객관식 보기에 들어갈 수 있으며, 정답이 아니어도 괜찮습니다. "
                            + "단어의 사용법을 묻는 JLPT 스타일의 4지선다 문제를 만들어 주세요. "
                            + "문제 예시: <u>わざわざ</u>" +
                            "   객관식 예시" +
                            "   1. 準備した部屋は小さすぎて使えなかった。"
                            + " 2. となりに立っていた人が<u>わざわざ</u>私の足を踏んだ。"
                            + " 3. <u>わざわざ</u>ためたお金をどこかでなくしてしまった。"
                            + " 4. 彼はその映画を見るため<u>わざわざ</u>東京まで出かけた。"
                            + "이 형식으로 문제를 만들어 주세요. "
                            + "JSON 형식으로 다음과 같이 반환해 주세요: "
                            + "{ \"content\": \"문제\", \"options\": [\"1번\", \"2번\", \"3번\", \"4번\"], \"answer\": \"정답인것의 숫자만 표시\" } "
                            + "반드시 줄바꿈해서 보기 쉽게 보여주세요."
                            + " **일본어로만 문제와 객관식을 만들어 주세요** \"\n"
                            + "응답에 한국어나 영어 또는 이상한 문자는 절대 포함되지 않도록 주의해 주세요.";
            ;
            break;

        default:
            throw new IllegalArgumentException("잘못된 메시지 유형입니다.");
    }

    return List.of(new Message("user", prompt)); //프롬프트 내용들
}
}
