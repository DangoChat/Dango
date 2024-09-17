package com.dangochat.dango.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GPTRequest {
	
	private String model;
    private List<Message> messages;
    private int temperature;
    private int maxTokens;
    private int topP;
    private int frequencyPenalty;
    private int presencePenalty;

    // 기존 메시지 리스트와 새 프롬프트를 모두 받는 생성자
    public GPTRequest(String model,
                      List<Message> previousMessages, // 이전 대화 내용을 포함하는 리스트
                      String prompt,
                      int temperature,
                      int maxTokens,
                      int topP,
                      int frequencyPenalty,
                      int presencePenalty) {
        this.model = model;
        this.messages = new ArrayList<>(previousMessages); // 이전 대화 기록을 추가
        this.messages.add(new Message("user", prompt)); // 새로운 프롬프트 추가
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.topP = topP;
        this.frequencyPenalty = frequencyPenalty;
        this.presencePenalty = presencePenalty;
    }
}
