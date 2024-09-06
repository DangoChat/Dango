package com.dangochat.dango.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dangochat.dango.dto.MemberDTO;
import com.dangochat.dango.service.MemberService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("member")
@RequiredArgsConstructor
@Controller
public class MemberController {

	
	private final MemberService service;
	
	@GetMapping("loginForm")
	public String loginForm() {
		return "memberView/loginForm";
	}
	
	@GetMapping("join")
	public String join() {
		return"memberView/joinForm";
	}
	
	@PostMapping("join")
	public String join(@ModelAttribute MemberDTO member) {
		log.debug("전달된 회원정보 : {}", member);
		
		service.join(member);
		
		return "redirect:/";
	}

    //아이디 중복체크 컨트롤러
    @PostMapping("/idCheck")
	@ResponseBody
	public int idCheck(@RequestParam("id") String email) {
        // 서비스 메서드 호출하여 중복 여부 확인
        return service.idCheck(email);
		
	}
    
    
}
