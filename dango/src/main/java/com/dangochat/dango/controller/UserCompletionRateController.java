package com.dangochat.dango.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dangochat.dango.entity.MemberEntity;
import com.dangochat.dango.entity.UserCompletionRateEntity;
import com.dangochat.dango.security.AuthenticatedUser;
import com.dangochat.dango.service.MemberService;
import com.dangochat.dango.service.UserCompletionRateService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("CompletionRate")
public class UserCompletionRateController {

	private final UserCompletionRateService userCompletionRateService;
	private final MemberService memberService;
	
	
	 @GetMapping("/CompletionRateView")
	    public String CompletionRateView(@AuthenticationPrincipal AuthenticatedUser userDetails, Model model) {
	       
	        return "CompletionRateView/CompletionRateView";  // HTML 파일 경로
	    }
	
	
	
	 @GetMapping("/rank")
	    public String getUserRank(@AuthenticationPrincipal AuthenticatedUser userDetails, Model model) {
	       
	        return "CompletionRateView/rankView";
	    }
	 
	 
	 
	 
}
