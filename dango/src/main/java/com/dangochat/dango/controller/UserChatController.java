package com.dangochat.dango.controller;

import com.dangochat.dango.entity.MemberEntity;
import com.dangochat.dango.security.AuthenticatedUser;
import com.dangochat.dango.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Slf4j
@Controller
@RequestMapping("userChat")
@RequiredArgsConstructor
public class UserChatController {

    private final MemberService memberService;

    @GetMapping("TestMileageChat")
    public String mileageChatTest() {
        return "UserMileageChat/testChat";  // 파일이 templates/UserMileageChat 폴더에 있어야 함
    }
    
    @PostMapping("TestMileageChat")
    @ResponseBody
    public String decreaseMileage(@AuthenticationPrincipal AuthenticatedUser userDetails) {
        int userId = userDetails.getId(); // AuthenticatedUser에서 유저 ID를 가져옴
        MemberEntity member = memberService.findById(userId);
        if (member != null && member.getUserMileage() >= 5) {
            member.setUserMileage(member.getUserMileage() - 5);
            memberService.save(member);
            return "success";
        }
        return "fail";
    }
}
