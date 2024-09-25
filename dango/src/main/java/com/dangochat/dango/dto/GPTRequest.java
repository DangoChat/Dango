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
	private double temperature;      //텍스트 생성의 랜덤성을 제거하는 값입니다. 값이 높을수록 결과가 더 다양해집니다.
	private int maxTokens;      //생성할 응답의 최대길이 (토큰 수)
	private int topP;       // 값이 낮을수록 결과가 더 예측가능
	private int frequencyPenalty;   //동일한 단어나 문구가 반복적으로되는것을 방지 
	private int presencePenalty;   

	// 기존 메시지 리스트와 새 프롬프트를 모두 받는 생성자
	public GPTRequest(String model, List<Message> previousMessages, // 이전 대화 내용을 포함하는 리스트
			String prompt, double temperature, int maxTokens, int topP, int frequencyPenalty, int presencePenalty) {
		this.model = model;
		this.messages = new ArrayList<>(previousMessages); // 이전 대화 기록을 추가

// prompt가 null이 아닌 경우에만 새로운 메시지를 추가
		if (prompt != null && !prompt.trim().isEmpty()) {
			this.messages.add(new Message("user", prompt)); // 새로운 프롬프트 추가
		}

		this.temperature = temperature;
		this.maxTokens = maxTokens;
		this.topP = topP;
		this.frequencyPenalty = frequencyPenalty;
		this.presencePenalty = presencePenalty;
	}
}
