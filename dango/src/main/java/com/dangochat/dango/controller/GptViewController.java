package com.dangochat.dango.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("gptChat")
public class GptViewController {

	// HTML 파일을 반환하는 메서드
    @GetMapping("gptChat")
    public String gptChatView() {
        // src/main/resources/templates/GPTChat/GPTChat.html을 가리킵니다.
        return "GPTChat/GPTChat";
    }
}
