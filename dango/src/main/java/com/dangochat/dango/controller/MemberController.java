package com.dangochat.dango.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
	
	
	// 비밀번호 찾기 폼을 보여주는 메서드
    @GetMapping("passwordSearch")
    public String passwordSearch() {
        return "memberView/passwordsearchForm";
    }
	
    
    @PostMapping("passwordSearch")
    public String passwordSearch(@RequestParam("userEmail") String email, Model model) {
    	log.debug("{}" ,email);
        boolean emailSent = service.sendPasswordResetLink(email);
        if (emailSent) {
            System.out.println("성공");
        } else {
        	System.out.println("실패");
        }
        return "redirect:/";    //"memberView/passwordsearchForm" 이쪽으로도 가능
    }
    
    
    @GetMapping("resetPassword")
    public String resetPassword() {
        return "memberView/resetPasswordForm";
    }
    
    
    @PostMapping("resetPassword")
    public String resetPassword(@RequestParam("token") String token, 
                                @RequestParam("newPassword") String newPassword, 
                                Model model) {
        boolean resetSuccessful = service.resetPassword(token, newPassword);
        if (resetSuccessful) {
            model.addAttribute("message", "비밀번호가 성공적으로 재설정되었습니다.");
            return "redirect:/member/login";
        } else {
            model.addAttribute("error", "비밀번호 재설정에 실패하였습니다.");
            return "memberView/resetPasswordForm";
        }
    }
    
    

    //아이디 중복체크 컨트롤러
    @PostMapping("/idCheck")
	@ResponseBody
	public int idCheck(@RequestParam("id") String id) {
		
		int cnt = service.idCheck(id);
		return cnt;
		
	}
    
    
}
