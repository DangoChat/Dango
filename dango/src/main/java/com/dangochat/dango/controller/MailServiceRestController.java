package com.dangochat.dango.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller; // RestController 대신 Controller 사용
import org.springframework.web.bind.annotation.*;

import com.dangochat.dango.service.MailService;
import com.dangochat.dango.service.MailServiceImpl;

@Controller  // @RestController 대신 @Controller 사용
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/mail")
public class MailServiceRestController {

    private final MailServiceImpl mailService;

    // HTML 뷰를 반환하는 메서드 (뷰 템플릿)
    @GetMapping("passwordSearch2")
    public String passwordSearch2() {
        return "memberView/passwordsearchForm2";  // HTML 템플릿 반환
    }
    
    // 이메일 전송 메서드에 @ResponseBody 추가하여 JSON 반환
    @PostMapping("passwordSearch2")
    @ResponseBody  // 이 메서드는 JSON 응답을 반환함
    public void passwordSearch2(@RequestParam("userEmail") String email) throws Exception {
    	System.out.println(email);
        String code = mailService.sendSimpleMessage(email);
        log.info("사용자에게 발송한 인증코드 ==> " + code);
    }

    // 인증 코드 검증 메서드에 @ResponseBody 추가하여 JSON 반환
    @GetMapping("/verifications")
    @ResponseBody  // 이 메서드는 JSON 응답을 반환함
    public ResponseEntity<?> verificationEmail(@RequestParam("code") String code) {
        return ResponseEntity.ok().body(mailService.verifyCode(code));
    }
}
