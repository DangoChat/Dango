package com.dangochat.dango.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dangochat.dango.service.StudyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("gptChat")
@RequiredArgsConstructor
public class GptViewController {

	private final StudyService studyService;
	
	// HTML 파일을 반환하는 메서드
    @GetMapping("gptChat")
    public String gptChatView() {
        // src/main/resources/templates/GPTChat/GPTChat.html을 가리킵니다.
        return "GPTChat/GPTChat";
    }
    
    
    @GetMapping("gptStudyChat")
    public String gptStudyChatView() {

        return "GPTChat/GPTStudyChat";
    }

 
}
