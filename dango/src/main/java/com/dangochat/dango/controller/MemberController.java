package com.dangochat.dango.controller;

import com.dangochat.dango.entity.MemberEntity;
import com.dangochat.dango.repository.MemberRepository;
import com.dangochat.dango.security.AuthenticatedUser;
import com.dangochat.dango.service.UserMileageService;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dangochat.dango.dto.MemberDTO;
import com.dangochat.dango.service.MemberService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("member")
@RequiredArgsConstructor
public class MemberController {

	
	private final MemberService service;
	private final UserMileageService userMileageService;

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
	//마일리지 점수 확인
	@GetMapping("/mileage")
	public String getMileage(@AuthenticationPrincipal AuthenticatedUser userDetails, Model model) {

		//마일리지 서비스에서 로그인된 사용자의 아이디 가져와서 마일리지에 넣기
		int mileageAmount = userMileageService.getUserMileageAmount(userDetails.getId());

		// 마일리지 정보를 모델에 추가
		model.addAttribute("mileageAmount", mileageAmount);

		// 마일리지를 보여줄 뷰로 반환 (뷰 이름은 "mileage-view"로 가정)
		return "Mileage";
	}

	
	
	@GetMapping("levelSetting")
	public String getLevelSettingPage(@AuthenticationPrincipal AuthenticatedUser userDetails, Model model) {
	    // 현재 사용자 레벨 정보를 가져옴
	    String currentLevel = service.getUserCurrentLevel(userDetails.getId());
	    String originalLevel = service.getOriginalLevel(userDetails.getId());
	    
	    // 유저의 nationality 정보 가져오기
	    String userNationality = service.getUserNationality(userDetails.getId());
	    
	    // 모델에 현재 레벨 정보와 originalLevel을 추가
	    model.addAttribute("currentLevel", currentLevel);
	    model.addAttribute("originalLevel", originalLevel);
	    model.addAttribute("userNationality", userNationality);     //HTML에 유저 국적을 알려주는 모델. 나중에 해당 국적이 아니면 버튼이 안보이게 가능
	    
	    // "levelSetting.html"로 이동
	    return "memberView/levelSetting";
	}
	
	
	
	




	
}
