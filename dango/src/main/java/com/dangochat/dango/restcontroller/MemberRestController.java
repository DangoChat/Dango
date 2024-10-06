package com.dangochat.dango.restcontroller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.dangochat.dango.dto.MemberDTO;
import com.dangochat.dango.entity.MemberEntity;
import com.dangochat.dango.repository.MemberRepository;
import com.dangochat.dango.security.AESUtil;
import com.dangochat.dango.security.AuthenticatedUser;
import com.dangochat.dango.service.MemberService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberRestController {

    private final AuthenticationManager authenticationManager;
    private final MemberService memberService;
    private final MemberRepository memberRepository;

    // 로그인 API
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) { // @RequestBody로 변경
        String userEmail = loginRequest.getUserEmail();
        String userPassword = loginRequest.getUserPassword();

        try {
            log.debug("login test ID: {} ,PW : {}", userEmail, userPassword);
            // 사용자 정보 조회
            MemberEntity memberEntity = memberRepository.findByUserEmail(userEmail);
            if (memberEntity == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid login credentials 1");
            }

            // AES로 암호화된 비밀번호 복호화
            String decryptedPassword;
            try {
                decryptedPassword = AESUtil.decrypt(memberEntity.getUserPassword());
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Password decryption failed 2");
            }
            log.debug("userPassword : {} / decryptedPW  : {}", userPassword, decryptedPassword);
            // 입력된 비밀번호와 복호화된 비밀번호를 비교
            if (!decryptedPassword.equals(userPassword)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid login credentials 2");
            }

            // 인증 처리
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    userEmail,
                    userPassword // 평문 비밀번호로 인증 처리
                )
            );

            // 인증 성공 시 SecurityContext에 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("Authentication Principal: " + authentication.getPrincipal().getClass().getName());
            AuthenticatedUser userDetails = (AuthenticatedUser) authentication.getPrincipal();
            MemberDTO userInfo = memberService.getMemberInfo(userDetails.getEmail());
            log.debug("Authentication 성공:111111 " + authentication.getPrincipal());

            // 로그인 성공 응답
            return ResponseEntity.ok(userInfo);

        } catch (AuthenticationException e) {
            // 인증 실패 시 401 Unauthorized 응답
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid login credentials 3");
        }
    }



    // 회원가입 API
    @PostMapping("/join")
    public ResponseEntity<?> join(@RequestBody MemberDTO member) {
        log.debug("전달된 회원정보 : {}", member);
        
        memberService.join(member);
        
        return ResponseEntity.status(HttpStatus.CREATED).body("User successfully registered");
    }

    @PostMapping("/mileage")
    public Integer getUserMileage(@RequestBody Map<String, Object> payload) {
        Integer userId = (Integer) payload.get("userId");
        int mileage = memberService.getUserMileage(userId);
        return mileage;
    }
    
    // 로그인 요청을 위한 DTO
    @Data
    public static class LoginRequest {
        private String userEmail;
        private String userPassword;
    }
    
    
    
    
    
 // 현재 레벨 정보 가져오는 Controller
    @GetMapping("/levelSetting")
    public ResponseEntity<Map<String, Object>> getLevelSetting(@AuthenticationPrincipal AuthenticatedUser userDetails) {
        Map<String, Object> response = new HashMap<>();
        String currentLevel = memberService.getUserCurrentLevel(userDetails.getId());
        String originalLevel = memberService.getOriginalLevel(userDetails.getId());
        String userNationality = memberService.getUserNationality(userDetails.getId());

        response.put("currentLevel", currentLevel);
        response.put("originalLevel", originalLevel);
        response.put("userNationality", userNationality);

        return ResponseEntity.ok(response);
    }

    // 레벨 업데이트 API
    @PostMapping("/levelSetting")
    public ResponseEntity<Map<String, Object>> levelSetting(@RequestBody Map<String, Object> request,
                                                            @AuthenticationPrincipal AuthenticatedUser userDetails) {
        String level = (String) request.get("level");
        boolean updateBoth = (boolean) request.get("updateBoth");

        String currentLevel = memberService.getUserCurrentLevel(userDetails.getId());
        String originalLevel = memberService.getOriginalLevel(userDetails.getId());

        if (!updateBoth && isInvalidHigherLevelChange(originalLevel, level)) {
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("error", "original_level보다 하위로만 이동할 수 없습니다.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseBody);
        }

        if (updateBoth) {
            memberService.updateUserLevels(userDetails.getId(), level, level);
        } else {
            memberService.updateCurrentLevel(userDetails.getId(), level);
        }

        currentLevel = memberService.getUserCurrentLevel(userDetails.getId());
        originalLevel = memberService.getOriginalLevel(userDetails.getId());

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "레벨이 성공적으로 업데이트되었습니다.");
        responseBody.put("currentLevel", currentLevel);
        responseBody.put("originalLevel", originalLevel);

        return ResponseEntity.ok(responseBody);
    }

    private boolean isInvalidHigherLevelChange(String originalLevel, String level) {
        // 원래의 레벨보다 높은 레벨로 변경이 가능한지 확인하는 로직
        return false;
    }
    
    
    
}
