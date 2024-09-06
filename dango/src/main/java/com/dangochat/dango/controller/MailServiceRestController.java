package com.dangochat.dango.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.stereotype.Controller; // RestController 대신 Controller 사용
import org.springframework.web.bind.annotation.*;

import com.dangochat.dango.repository.MemberRepository;
import com.dangochat.dango.service.MailService;

import java.util.Map;
import java.util.HashMap;

@Controller  // @RestController 대신 @Controller 사용
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/mail")
public class MailServiceRestController {

    private final MailService mailService;
    private final MemberRepository memberRepository;

    // HTML 뷰를 반환하는 메서드 (뷰 템플릿)
    @GetMapping("passwordSearch2")
    public String passwordSearch2() {
        return "memberView/passwordsearchForm2";  // HTML 템플릿 반환
    }
    
    // 이메일 전송 메서드에 @ResponseBody 추가하여 JSON 반환
    @PostMapping("passwordSearch2")
    @ResponseBody  // 이 메서드는 JSON 응답을 반환함
    public Map<String, String> passwordSearch2(@RequestBody Map<String, String> requestData) throws Exception {
        String email = requestData.get("userEmail"); // JSON에서 "userEmail" 값을 가져옴
        System.out.println(email);
        Map<String, String> response = new HashMap<>();
        if(memberRepository.existsByUserEmail(email)){
            mailService.sendSimpleMessage(email);
            response.put("message", "비밀번호가 발송되었습니다.");
            return response;
        }
        else{
            response.put("message", "없는 아이디 입니다.");
            return response;
        }
    }

}
