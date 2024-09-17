package com.dangochat.dango.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("tts")
@RequiredArgsConstructor
public class TTSTestController {

	
	@GetMapping("ttsTest")
	public String ttsTest() {
		
		return "TTSTest/TTSTest";
	}
	
}
