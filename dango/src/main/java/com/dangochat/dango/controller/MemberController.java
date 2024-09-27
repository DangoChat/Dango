package com.dangochat.dango.controller;

import com.dangochat.dango.entity.MemberEntity;
import com.dangochat.dango.repository.MemberRepository;
import com.dangochat.dango.security.AuthenticatedUser;
import com.dangochat.dango.service.UserMileageService;

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
	public String levelSetting() {
		return"memberView/levelSetting";
	}
	
	
	@PostMapping("levelSetting")                               // 키값이 String 벨류값이 Object
	public ResponseEntity<String> levelSetting(@RequestBody Map<String, Object> request, @AuthenticationPrincipal AuthenticatedUser userDetails) {
	    String level = (String) request.get("level");
	    boolean updateBoth = (boolean) request.get("updateBoth");

	    // 현재 사용자의 레벨을 가져옴
	    String currentLevel = service.getUserCurrentLevel(userDetails.getId());
	    String originalLevel = service.getOriginalLevel(userDetails.getId()); // original_level 값도 가져옴

	    // 두 번째 줄 버튼: current_level은 original_level보다 하위로만 설정 가능
	    if (!updateBoth && isInvalidHigherLevelChange(originalLevel, level)) {
	        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("original_level보다 하위로만 이동할 수 있습니다.");
	    }
	    
	    // 레벨 업데이트
	    if (updateBoth) {
	        // 첫 번째 줄 버튼: current_level과 original_level 둘 다 업데이트 (제한 없음)
	        service.updateUserLevels(userDetails.getId(), level, level);
	    } else {
	        // 두 번째 줄 버튼: current_level만 업데이트 (original_level보다 상위 레벨만 설정 가능)
	        service.updateCurrentLevel(userDetails.getId(), level);
	    }

	    return ResponseEntity.ok("레벨이 성공적으로 업데이트되었습니다.");
	}

	// current_level이 original_level보다 하위로 이동하지 못하게 제한
	private boolean isInvalidHigherLevelChange(String originalLevel, String targetLevel) {
	    Map<String, Integer> levelMap = Map.of("N1", 1, "N2", 2, "N3", 3, "N4", 4, "N5", 5);
	    // original_level보다 하위로 가려는 경우 true 반환 (상위로만 이동 가능)
	    return levelMap.get(originalLevel) > levelMap.get(targetLevel);
	}




	
}
