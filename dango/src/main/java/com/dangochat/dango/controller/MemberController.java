package com.dangochat.dango.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("member")
@RequiredArgsConstructor
@Controller
public class MemberController {

	
	
	@GetMapping("join")
	public String join() {
		return"memberView/joinForm";
	}
	
}
